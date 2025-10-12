package com.sloyardms.backend.user_filter.service;

import com.sloyardms.backend.common.error.ResourceAlreadyExistsException;
import com.sloyardms.backend.common.error.ResourceNotFoundException;
import com.sloyardms.backend.user.UserRepository;
import com.sloyardms.backend.user.entity.User;
import com.sloyardms.backend.user_filter.UserFilterRepository;
import com.sloyardms.backend.user_filter.dto.UserFilterCreateDto;
import com.sloyardms.backend.user_filter.dto.UserFilterDto;
import com.sloyardms.backend.user_filter.dto.UserFilterUpdateDto;
import com.sloyardms.backend.user_filter.entity.UserFilter;
import com.sloyardms.backend.user_filter.mapper.UserFilterMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class UserFilterService implements IUserFilterService {

    private final UserFilterRepository userFilterRepository;
    private final UserFilterMapper userFilterMapper;
    private final UserRepository userRepository;

    public UserFilterService(UserFilterRepository userFilterRepository, UserFilterMapper userFilterMapper,
                             UserRepository userRepository) {
        this.userFilterRepository = userFilterRepository;
        this.userFilterMapper = userFilterMapper;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public UserFilterDto getById(UUID id, UUID userExternalId) {
        UserFilter userFilter =
                userFilterRepository.findByIdAndUserExternalId(id, userExternalId).orElseThrow(
                        () -> new ResourceNotFoundException("UserFilter", "id", id));
        return userFilterMapper.toDto(userFilter);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserFilterDto> getUserFilters(UUID userExternalId, Pageable pageable) {
        Page<UserFilter> filterList = userFilterRepository.findByUser(userExternalId, pageable);
        return filterList.map(userFilterMapper::toDto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserFilterDto create(UUID userExternalId, UserFilterCreateDto userFilterCreateDto) {
        User foundUser =
                userRepository.findByExternalId(userExternalId).orElseThrow(() -> new ResourceNotFoundException("User"
                        , "External Id", userExternalId));

        UserFilter userFilter = userFilterMapper.toEntity(userFilterCreateDto);
        userFilter.setUser(foundUser);
        userFilter.setId(UUID.randomUUID());

        return saveFilterChangesInDb(userFilter);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserFilterDto update(UUID id, UUID userExternalId, UserFilterUpdateDto userFilterUpdateDto) {
        UserFilter userFilter =
                userFilterRepository.findByIdAndUserExternalId(id, userExternalId).orElseThrow(
                        () -> new ResourceNotFoundException("UserFilter", "Id", id));

        UserFilter updatedFilter = userFilterMapper.updateFromDto(userFilterUpdateDto, userFilter);
        return saveFilterChangesInDb(updatedFilter);
    }

    private UserFilterDto saveFilterChangesInDb(UserFilter userFilter) {
        try {
            UserFilter savedFilter = userFilterRepository.save(userFilter);
            return userFilterMapper.toDto(savedFilter);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (message != null && message.contains("user_filter_url_pattern_unique")) {
                throw new ResourceAlreadyExistsException("UserFilter", "Url Pattern", userFilter.getUrlPattern());
            }
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(UUID id, UUID userExternalId) {
        UserFilter filter =
                userFilterRepository.findByIdAndUserExternalId(id, userExternalId).orElseThrow(
                        () -> new ResourceNotFoundException("UserFilter", "Id", id));
        userFilterRepository.delete(filter);
    }
}
