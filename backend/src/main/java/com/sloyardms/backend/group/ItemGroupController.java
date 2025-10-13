package com.sloyardms.backend.group;

import com.sloyardms.backend.group.dto.ItemGroupCreateDto;
import com.sloyardms.backend.group.dto.ItemGroupDto;
import com.sloyardms.backend.group.dto.ItemGroupUpdateDto;
import com.sloyardms.backend.group.service.IItemGroupService;
import com.sloyardms.backend.security.util.AuthUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/api/v1/groups")
public class ItemGroupController {

    private final IItemGroupService service;

    public ItemGroupController(IItemGroupService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemGroupDto> getById(@PathVariable UUID id) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        ItemGroupDto result = service.getById(id, userExternalId);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Page<ItemGroupDto>> getAllUserGroups(
            @PageableDefault(size = 15, page = 0, direction = Sort.Direction.ASC)
            Pageable pageable) {

        Sort sort = Sort.by(
                Sort.Order.desc("defaultGroup"),
                Sort.Order.asc("name")
        );
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        Page<ItemGroupDto> groupList = service.getUserGroups(userExternalId, sortedPageable);
        return ResponseEntity.ok(groupList);
    }

    @PostMapping
    public ResponseEntity<ItemGroupDto> create(@Valid @RequestBody ItemGroupCreateDto itemGroupCreateDto) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        ItemGroupDto result = service.create(userExternalId, itemGroupCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemGroupDto> update(@PathVariable UUID id,
                                               @Valid @RequestBody ItemGroupUpdateDto itemGroupUpdateDto) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        ItemGroupDto result = service.update(id, userExternalId, itemGroupUpdateDto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ItemGroupDto> delete(@PathVariable UUID id) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        service.delete(id, userExternalId);
        return ResponseEntity.noContent().build();
    }
}
