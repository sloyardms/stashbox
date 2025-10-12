package com.sloyardms.backend.user;


import com.sloyardms.backend.user.dto.UserDetailDto;
import com.sloyardms.backend.user.dto.UserSummaryDto;
import com.sloyardms.backend.user.service.IUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private final IUserService service;

    public UserAdminController(IUserService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailDto> getById(@PathVariable UUID userId) {
        UserDetailDto result = service.getById(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Page<UserSummaryDto>> getAllUsers(
            @PageableDefault(size = 15, page = 0, sort = "userName", direction = Sort.Direction.ASC)
            Pageable pageable) {
        Page<UserSummaryDto> userList = service.getAllUsers(pageable);
        return ResponseEntity.ok(userList);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        service.delete(userId);
        return ResponseEntity.noContent().build();
    }

    @Profile("dev")
    @GetMapping("dev-user")
    public ResponseEntity<UserDetailDto> getDevUser() {
        UserDetailDto result = service.getDevUser();
        return ResponseEntity.ok(result);
    }
}
