package com.sloyardms.stashbox.userfilter.controller;

import com.sloyardms.stashbox.security.utils.AuthUtils;
import com.sloyardms.stashbox.userfilter.dto.CreateUserFilterRequest;
import com.sloyardms.stashbox.userfilter.dto.UpdateUserFilterRequest;
import com.sloyardms.stashbox.userfilter.dto.UserFilterResponse;
import com.sloyardms.stashbox.userfilter.service.UserFilterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/v1/filters")
public class UserFilterController {

    private final UserFilterService userFilterService;

    @GetMapping("/{id}")
    public ResponseEntity<UserFilterResponse> getUserFilterById(@PathVariable UUID id) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        UserFilterResponse response = userFilterService.getUserFilterById(id, userExternalId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UserFilterResponse>> getAllUserFilters(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        Page<UserFilterResponse> response = userFilterService.getAllUserFilters(search, active, domain, userExternalId,
                pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserFilterResponse> saveUserFilter(@Valid @RequestBody CreateUserFilterRequest request) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        UserFilterResponse response = userFilterService.saveUserFilter(request, userExternalId);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserFilterResponse> updateUserFilter(@PathVariable UUID id,
                                                               @Valid @RequestBody UpdateUserFilterRequest request) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        UserFilterResponse response = userFilterService.updateUserFilter(id, request, userExternalId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserFilter(@PathVariable UUID id) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        userFilterService.deleteUserFilter(id, userExternalId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/match")
    public ResponseEntity<Void> recordMatch(@PathVariable UUID id) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        userFilterService.recordFilterMatch(id, userExternalId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/domains")
    public ResponseEntity<List<String>> getUserFilterDomains() {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        List<String> userFilterDomains = userFilterService.getUserFilterDomains(userExternalId);
        return ResponseEntity.ok(userFilterDomains);
    }

}
