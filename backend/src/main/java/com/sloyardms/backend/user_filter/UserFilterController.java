package com.sloyardms.backend.user_filter;

import com.sloyardms.backend.security.util.AuthUtils;
import com.sloyardms.backend.user_filter.dto.UserFilterCreateDto;
import com.sloyardms.backend.user_filter.dto.UserFilterDto;
import com.sloyardms.backend.user_filter.dto.UserFilterUpdateDto;
import com.sloyardms.backend.user_filter.service.IUserFilterService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/me/filters")
public class UserFilterController {

    private IUserFilterService service;

    public UserFilterController(IUserFilterService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserFilterDto> getById(@PathVariable UUID id) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        UserFilterDto userFilterDto = service.getById(id, userExternalId);
        return ResponseEntity.ok(userFilterDto);
    }

    @GetMapping
    public ResponseEntity<Page<UserFilterDto>> findUserFilters(
            @PageableDefault(size = 15, page = 0, sort = "filterName", direction = Sort.Direction.ASC)
            Pageable pageable) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        Page<UserFilterDto> filterList = service.getUserFilters(userExternalId, pageable);
        return ResponseEntity.ok(filterList);
    }

    @PostMapping
    public ResponseEntity<UserFilterDto> create(@Valid @RequestBody UserFilterCreateDto userFilterCreateDto) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        UserFilterDto createdFilter = service.create(userExternalId, userFilterCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFilter);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserFilterDto> patch(@PathVariable UUID id,
                                               @Valid @RequestBody UserFilterUpdateDto userFilterUpdateDto) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        UserFilterDto updatedFilter = service.update(id, userExternalId, userFilterUpdateDto);
        return ResponseEntity.ok(updatedFilter);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        service.delete(id, userExternalId);
        return ResponseEntity.noContent().build();
    }

}
