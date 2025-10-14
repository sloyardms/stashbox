package com.sloyardms.backend.tag;

import com.sloyardms.backend.security.util.AuthUtils;
import com.sloyardms.backend.tag.dto.TagCreateDto;
import com.sloyardms.backend.tag.dto.TagDto;
import com.sloyardms.backend.tag.dto.TagUpdateDto;
import com.sloyardms.backend.tag.service.ITagService;
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
@RequestMapping("/api/v1/tags")
public class TagController {

    private final ITagService service;

    public TagController(ITagService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDto> getById(@PathVariable UUID id) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        TagDto result = service.getById(id, userExternalId);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Page<TagDto>> getAll(
            @PageableDefault(size = 15, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        Page<TagDto> tagList = service.getAllTags(userExternalId, pageable);
        return ResponseEntity.ok(tagList);
    }

    @PostMapping
    public ResponseEntity<TagDto> create(@Valid @RequestBody TagCreateDto tagCreateDto) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        TagDto result = service.create(userExternalId, tagCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TagDto> update(@PathVariable UUID id, @Valid @RequestBody TagUpdateDto tagUpdateDto) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        TagDto result = service.update(id, userExternalId, tagUpdateDto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TagDto> delete(@PathVariable UUID id) {
        UUID userExternalId = AuthUtils.getCurrentUserExternalId();
        service.delete(id, userExternalId);
        return ResponseEntity.noContent().build();
    }

}
