package com.sloyardms.stashbox.userfilter.service;

import com.sloyardms.stashbox.common.error.exception.ResourceAlreadyExistsException;
import com.sloyardms.stashbox.common.error.exception.ResourceNotFoundException;
import com.sloyardms.stashbox.common.utils.PageableValidator;
import com.sloyardms.stashbox.user.entity.User;
import com.sloyardms.stashbox.user.repository.UserRepository;
import com.sloyardms.stashbox.userfilter.dto.CreateUserFilterRequest;
import com.sloyardms.stashbox.userfilter.dto.UpdateUserFilterRequest;
import com.sloyardms.stashbox.userfilter.dto.UserFilterResponse;
import com.sloyardms.stashbox.userfilter.entity.UserFilter;
import com.sloyardms.stashbox.userfilter.mapper.UserFilterMapper;
import com.sloyardms.stashbox.userfilter.repository.UserFilterRepository;
import com.sloyardms.stashbox.userfilter.specification.UserFilterSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserFilterServiceImpl implements UserFilterService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("filterName", "priority", "active", "matchCount",
            "createdAt", "updatedAt", "lastMatchedAt");
    private final UserFilterRepository userFilterRepository;
    private final UserFilterMapper userFilterMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserFilterResponse getUserFilterById(UUID id, UUID userExternalId) {
        UserFilter userFilter = userFilterRepository.findByIdAndUserExternalId(id, userExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("UserFilter", "id", id));
        System.out.println(userFilter);
        return userFilterMapper.toResponse(userFilter);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserFilterResponse> getAllUserFilters(String searchQuery, Boolean active, String domain,
                                                      UUID userExternalId,
                                                      Pageable pageable) {

        PageableValidator.validateSortFields(pageable, ALLOWED_SORT_FIELDS);
        Specification<UserFilter> spec = Specification.allOf(
                UserFilterSpecification.belongsToUser(userExternalId),
                UserFilterSpecification.active(active),
                UserFilterSpecification.byDomain(domain),
                UserFilterSpecification.search(searchQuery)
        );

        Page<UserFilter> filters = userFilterRepository.findAll(spec, pageable);
        return filters.map(userFilterMapper::toResponse);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserFilterResponse saveUserFilter(CreateUserFilterRequest request, UUID userExternalId) {
        User user = userRepository.findByExternalId(userExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "External Id", "[REDACTED]"));

        UserFilter filter = userFilterMapper.toEntity(request);
        filter.setUser(user);

        filter = saveChanges(filter);

        return userFilterMapper.toResponse(filter);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserFilterResponse updateUserFilter(UUID id, UpdateUserFilterRequest request, UUID userExternalId) {
        UserFilter filter = userFilterRepository.findByIdAndUserExternalId(id, userExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("UserFilter", "Id", id));
        filter = userFilterMapper.updateFromRequest(request, filter);
        filter = saveChanges(filter);
        return userFilterMapper.toResponse(filter);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteUserFilter(UUID id, UUID userExternalId) {
        UserFilter filter = userFilterRepository.findByIdAndUserExternalId(id, userExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("UserFilter", "Id", id));
        userFilterRepository.delete(filter);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recordFilterMatch(UUID id, UUID userExternalId) {
        int updated = userFilterRepository.incrementMatchCount(id, userExternalId, Instant.now());
        if (updated == 0) {
            throw new ResourceNotFoundException("UserFilter", "Id", id);
        }
    }

    @Override
    public List<String> getUserFilterDomains(UUID userExternalId) {
        return userFilterRepository.findDistinctDomainsByUserExternalId(userExternalId);
    }

    private UserFilter saveChanges(UserFilter userFilter) {
        try {
            return userFilterRepository.save(userFilter);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (message != null && message.contains("user_filters_normalized_filter_name_unique")) {
                throw new ResourceAlreadyExistsException("UserFilter", "Filter Name", userFilter.getFilterName());
            }
            if (message != null && message.contains("user_filters_normalized_url_pattern_unique")) {
                throw new ResourceAlreadyExistsException("UserFilter", "Url Pattern", userFilter.getUrlPattern());
            }
            throw e;
        }
    }

}
