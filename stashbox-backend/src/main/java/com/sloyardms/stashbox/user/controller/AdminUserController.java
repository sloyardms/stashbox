package com.sloyardms.stashbox.user.controller;

import com.sloyardms.stashbox.security.utils.AuthUtils;
import com.sloyardms.stashbox.user.dto.AdminUserResponse;
import com.sloyardms.stashbox.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public Page<AdminUserResponse> getUsersExcludingCurrent(
            @RequestParam(required = false) String search,
            @SortDefault(sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        UUID authenticatedUserExternalId = AuthUtils.getCurrentUserExternalId();
        return userService.getAllUsersExcludingCurrent(authenticatedUserExternalId, search, pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUserByInternalId(id);
        return ResponseEntity.noContent().build();
    }

}
