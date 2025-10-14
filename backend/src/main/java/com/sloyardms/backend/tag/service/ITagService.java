package com.sloyardms.backend.tag.service;

import com.sloyardms.backend.tag.dto.TagCreateDto;
import com.sloyardms.backend.tag.dto.TagDto;
import com.sloyardms.backend.tag.dto.TagUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ITagService {

    TagDto getById(UUID id, UUID userExternalId);

    Page<TagDto> getAllTags(UUID userExternalId, Pageable pageable);

    TagDto create(UUID userExternalId, TagCreateDto tagCreateDto);

    TagDto update(UUID id, UUID userExternalId, TagUpdateDto tagUpdateDto);

    void delete(UUID id, UUID userExternalId);

}
