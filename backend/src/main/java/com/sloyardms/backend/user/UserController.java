package com.sloyardms.backend.user;

import com.sloyardms.backend.security.util.AuthUtils;
import com.sloyardms.backend.user.dto.UserDetailDto;
import com.sloyardms.backend.user.dto.UserSettingsUpdateDto;
import com.sloyardms.backend.user.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/me")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDetailDto> getByExternalId() {
        UUID externalId = AuthUtils.getCurrentUserExternalId();
        UserDetailDto result = userService.getByExternalId(externalId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<UserDetailDto> create() {
        UUID externalId = AuthUtils.getCurrentUserExternalId();
        String username = AuthUtils.getCurrentUsername();
        UserDetailDto createdUser = userService.create(externalId, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PatchMapping("/settings")
    public ResponseEntity<UserDetailDto> updateSettings(@Valid @RequestBody UserSettingsUpdateDto userSettingsUpdateDto) {
        UUID externalId = AuthUtils.getCurrentUserExternalId();
        UserDetailDto updatedUser = userService.updateSettings(externalId, userSettingsUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete() {
        UUID externalId = AuthUtils.getCurrentUserExternalId();
        userService.deleteByExternalId(externalId);
        return ResponseEntity.noContent().build();
    }

}
