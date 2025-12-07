package com.sloyardms.stashbox.user.service;

import com.sloyardms.stashbox.user.dto.AdminUserResponse;
import com.sloyardms.stashbox.user.dto.UpdateUserSettingsRequest;
import com.sloyardms.stashbox.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    UserResponse getUserByExternalId(UUID userExternalId);

    Page<AdminUserResponse> getAllUsersExcludingCurrent(UUID currentAdminExternalId, String searchQuery,
                                                        Pageable pageable);

    UserResponse saveUser(UUID externalId, String username, String email);

    void deleteUserByInternalId(UUID internalId);

    void deleteUserByExternalId(UUID userExternalId);

    UserResponse updateUserSettings(UpdateUserSettingsRequest updateUserSettingsRequest, UUID userExternalId);

}
