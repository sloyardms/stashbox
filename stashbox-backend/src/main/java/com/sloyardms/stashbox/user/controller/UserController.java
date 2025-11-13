package com.sloyardms.stashbox.user.controller;

import com.sloyardms.stashbox.security.utils.AuthUtils;
import com.sloyardms.stashbox.user.dto.UpdateUserSettingsRequest;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/v1/users/me")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getCurrentUser() {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        UserResponse result = userService.getUserByExternalId(userExternalId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<UserResponse> saveUser() {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        String username = AuthUtils.getCurrentUsername();
        String email = AuthUtils.getCurrentUserEmail();
        UserResponse result = userService.saveUser(userExternalId, username, email);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();
        return ResponseEntity.created(location).body(result);
    }

    @PatchMapping("/settings")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UpdateUserSettingsRequest request) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        UserResponse response = userService.updateUserSettings(request, userExternalId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser() {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        userService.deleteUserByExternalId(userExternalId);
        return ResponseEntity.noContent().build();
    }

}
