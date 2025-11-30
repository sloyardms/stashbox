package com.sloyardms.stashbox.user.service;

import com.sloyardms.stashbox.common.error.exception.ResourceAlreadyExistsException;
import com.sloyardms.stashbox.common.error.exception.ResourceNotFoundException;
import com.sloyardms.stashbox.common.utils.PageableValidator;
import com.sloyardms.stashbox.user.dto.AdminUserResponse;
import com.sloyardms.stashbox.user.dto.UpdateUserSettingsRequest;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.user.entity.User;
import com.sloyardms.stashbox.user.entity.UserSettings;
import com.sloyardms.stashbox.user.mapper.UserMapper;
import com.sloyardms.stashbox.user.mapper.UserSettingsMapper;
import com.sloyardms.stashbox.user.repository.UserRepository;
import com.sloyardms.stashbox.user.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("username", "email", "createdAt", "updatedAt");
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserSettingsMapper userSettingsMapper;

    @Transactional(readOnly = true)
    @Override
    public UserResponse getUserByExternalId(UUID userExternalId) {
        User user = userRepository.findByExternalId(userExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "External Id", userExternalId));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AdminUserResponse> getAllUsersExcludingCurrent(UUID currentAdminExternalId, String searchQuery,
                                                               Pageable pageable) {

        PageableValidator.validateSortFields(pageable, ALLOWED_SORT_FIELDS);

        Specification<User> spec = Specification.allOf(
                UserSpecification.excludingUser(currentAdminExternalId),
                UserSpecification.search(searchQuery)
        );

        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(userMapper::toAdminResponse);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserResponse saveUser(UUID externalId, String username, String email) {
        User user = new User();
        user.setExternalId(externalId);
        user.setUsername(username);
        user.setEmail(email);

        user = saveUserChanges(user);
        return userMapper.toResponse(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteUserByInternalId(UUID internalId) {
        User user = userRepository.findById(internalId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", internalId));
        userRepository.delete(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteUserByExternalId(UUID userExternalId) {
        User user = userRepository.findByExternalId(userExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "External Id", userExternalId));
        userRepository.delete(user);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserResponse updateUserSettings(UpdateUserSettingsRequest updateUserSettingsRequest, UUID userExternalId) {
        User user = userRepository.findByExternalId(userExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "External Id", userExternalId));
        UserSettings newSettings = userSettingsMapper.updateFromDto(updateUserSettingsRequest, user.getSettings());
        user.setSettings(newSettings);
        user = saveUserChanges(user);
        return userMapper.toResponse(user);
    }

    private User saveUserChanges(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (message != null && message.contains("users_external_id_unique")) {
                throw new ResourceAlreadyExistsException("User", "External Id", user.getExternalId().toString());
            }
            if (message != null && message.contains("users_username_unique")) {
                throw new ResourceAlreadyExistsException("User", "Username", user.getUsername());
            }
            if (message != null && message.contains("users_username_email")) {
                throw new ResourceAlreadyExistsException("User", "Email", user.getEmail());
            }
            throw e;
        }
    }

}
