package com.sloyardms.backend.group.service;

import com.sloyardms.backend.group.dto.ItemGroupCreateDto;
import com.sloyardms.backend.group.dto.ItemGroupDto;
import com.sloyardms.backend.group.dto.ItemGroupUpdateDto;
import com.sloyardms.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IItemGroupService {

    ItemGroupDto getById(UUID id, UUID userExternalId);

    Page<ItemGroupDto> getUserGroups(UUID userExternalId, Pageable pageable);

    ItemGroupDto create(UUID userExternalId, ItemGroupCreateDto itemGroupCreateDto);

    void createDefaultGroup(User user);

    ItemGroupDto update(UUID id, UUID userExternalId, ItemGroupUpdateDto itemGroupUpdateDto);

    void delete(UUID id, UUID userExternalId);

}
