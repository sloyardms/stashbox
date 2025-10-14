package com.sloyardms.backend.tag.service;

import com.sloyardms.backend.common.error.ResourceAlreadyExistsException;
import com.sloyardms.backend.common.error.ResourceNotFoundException;
import com.sloyardms.backend.tag.TagRepository;
import com.sloyardms.backend.tag.dto.TagCreateDto;
import com.sloyardms.backend.tag.dto.TagDto;
import com.sloyardms.backend.tag.dto.TagUpdateDto;
import com.sloyardms.backend.tag.entity.Tag;
import com.sloyardms.backend.tag.mapper.TagMapper;
import com.sloyardms.backend.user.UserRepository;
import com.sloyardms.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TagService implements ITagService {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagService(UserRepository userRepository, TagRepository tagRepository, TagMapper tagMapper) {
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public TagDto getById(UUID id, UUID userExternalId) {
        Tag tag = tagRepository.findByIdAndUserExternalId(id, userExternalId).orElseThrow(
                () -> new ResourceNotFoundException("Tag", "Id", id));
        return tagMapper.toDto(tag);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TagDto> getAllTags(UUID userExternalId, Pageable pageable) {
        Page<Tag> tagList = tagRepository.findAllByUser(userExternalId, pageable);
        return tagList.map(tagMapper::toDto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TagDto create(UUID userExternalId, TagCreateDto tagCreateDto) {
        User foundUser = userRepository.findByExternalId(userExternalId).orElseThrow(
                () -> new ResourceNotFoundException("User", "External Id", userExternalId));

        Tag newTag = tagMapper.toEntity(tagCreateDto);
        newTag.setUser(foundUser);
        newTag.setId(UUID.randomUUID());

        return saveTagChangesInDb(newTag);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TagDto update(UUID id, UUID userExternalId, TagUpdateDto tagUpdateDto) {
        Tag tag = tagRepository.findByIdAndUserExternalId(id, userExternalId).orElseThrow(
                () -> new ResourceNotFoundException("Tag", "Id", id));

        Tag updatedTag = tagMapper.updateFromDto(tagUpdateDto, tag);
        return saveTagChangesInDb(updatedTag);
    }

    private TagDto saveTagChangesInDb(Tag tag) {
        try {
            Tag savedTag = tagRepository.save(tag);
            return tagMapper.toDto(savedTag);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("tags_user_id_name_unique")) {
                throw new ResourceAlreadyExistsException("Tag", "Name", tag.getName());
            }
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(UUID id, UUID userExternalId) {
        Tag tag = tagRepository.findByIdAndUserExternalId(id, userExternalId).orElseThrow(
                () -> new ResourceNotFoundException("Tag", "Id", id));
        tagRepository.delete(tag);
    }

}
