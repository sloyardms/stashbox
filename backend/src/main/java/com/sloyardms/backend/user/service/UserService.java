package com.sloyardms.backend.user.service;

import com.sloyardms.backend.common.error.ResourceAlreadyExistsException;
import com.sloyardms.backend.common.error.ResourceNotFoundException;
import com.sloyardms.backend.security.filter.FakeAuthFilter;
import com.sloyardms.backend.user.UserRepository;
import com.sloyardms.backend.user.dto.UserDetailDto;
import com.sloyardms.backend.user.dto.UserSettingsUpdateDto;
import com.sloyardms.backend.user.dto.UserSummaryDto;
import com.sloyardms.backend.user.entity.User;
import com.sloyardms.backend.user.entity.UserSettings;
import com.sloyardms.backend.user.mapper.UserMapper;
import com.sloyardms.backend.user.mapper.UserSettingsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserSettingsMapper userSettingsMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper,
                       UserSettingsMapper userSettingsMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userSettingsMapper = userSettingsMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetailDto getById(UUID id) {
        User foundUser = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "Id", id));
        return userMapper.toDetailDto(foundUser);
    }

    @Override
    public UserDetailDto getByExternalId(UUID externalId) {
        User foundUser = userRepository.findByExternalId(externalId).orElseThrow(
                () -> new ResourceNotFoundException("User", "External ID", externalId));
        return userMapper.toDetailDto(foundUser);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserSummaryDto> getAllUsers(Pageable pageable) {
        Page<User> userList = userRepository.findAll(pageable);
        return userList.map(userMapper::toSummaryDto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserDetailDto create(UUID externalId, String username) {
        User user = new User();
        user.setExternalId(externalId);
        user.setUserName(username);
        user.setId(UUID.randomUUID());

        return saveUserChangesInDb(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserDetailDto updateSettings(UUID externalId, UserSettingsUpdateDto userSettingsUpdateDto) {
        User user = userRepository.findByExternalId(externalId).orElseThrow(
                () -> new ResourceNotFoundException("User", "External Id", externalId));

        UserSettings newSettings = userSettingsMapper.updateFromDto(userSettingsUpdateDto, user.getSettings());
        user.setSettings(newSettings);

        return saveUserChangesInDb(user);
    }

    private UserDetailDto saveUserChangesInDb(User user) {
        try {
            User savedUser = userRepository.save(user);
            return userMapper.toDetailDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (message != null && message.contains("external_id")) {
                throw new ResourceAlreadyExistsException("User", "External Id", user.getExternalId().toString());
            }
            if (message != null && message.contains("user_name")) {
                throw new ResourceAlreadyExistsException("User", "Username", user.getUserName());
            }
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(UUID id) {
        userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "Id", id));
        userRepository.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByExternalId(UUID externalId) {
        userRepository.findByExternalId(externalId).orElseThrow(
                () -> new ResourceNotFoundException("User", "External ID", externalId));
        userRepository.deleteByExternalId(externalId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserDetailDto getDevUser() {
        UUID externalId = FakeAuthFilter.DEFAULT_EXTERNAL_USER_ID;
        Optional<User> foundUser = userRepository.findByExternalId(externalId);

        User userDb = null;
        if (foundUser.isPresent()) {
            userDb = foundUser.get();
        } else {
            User user = new User();
            user.setExternalId(externalId);
            user.setId(FakeAuthFilter.DEFAULT_USER_ID);
            user.setUserName(FakeAuthFilter.DEFAULT_USERNAME);

            userDb = userRepository.save(user);
        }

        return userMapper.toDetailDto(userDb);
    }

}

