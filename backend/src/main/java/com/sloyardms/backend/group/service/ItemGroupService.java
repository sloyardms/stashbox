package com.sloyardms.backend.group.service;

import com.sloyardms.backend.common.error.ResourceAlreadyExistsException;
import com.sloyardms.backend.common.error.ResourceNotFoundException;
import com.sloyardms.backend.group.ItemGroupRepository;
import com.sloyardms.backend.group.dto.ItemGroupCreateDto;
import com.sloyardms.backend.group.dto.ItemGroupDto;
import com.sloyardms.backend.group.dto.ItemGroupUpdateDto;
import com.sloyardms.backend.group.entity.ItemGroup;
import com.sloyardms.backend.group.mapper.ItemGroupMapper;
import com.sloyardms.backend.user.UserRepository;
import com.sloyardms.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ItemGroupService implements IItemGroupService {

    private UserRepository userRepository;
    private ItemGroupRepository itemGroupRepository;
    private final ItemGroupMapper itemGroupMapper;

    public ItemGroupService(ItemGroupRepository itemGroupRepository, ItemGroupMapper itemGroupMapper,
                            UserRepository userRepository) {
        this.userRepository = userRepository;
        this.itemGroupRepository = itemGroupRepository;
        this.itemGroupMapper = itemGroupMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public ItemGroupDto getById(UUID id, UUID userExternalId) {
        ItemGroup itemGroup = itemGroupRepository.findByIdAndUserExternalId(id, userExternalId).orElseThrow(
                () -> new ResourceNotFoundException("ItemGroup", "Id", id));
        return itemGroupMapper.toDto(itemGroup);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ItemGroupDto> getUserGroups(UUID userExternalId, Pageable pageable) {
        Page<ItemGroup> groupList = itemGroupRepository.findAllByUser(userExternalId, pageable);
        return groupList.map(itemGroupMapper::toDto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ItemGroupDto create(UUID userExternalId, ItemGroupCreateDto itemGroupCreateDto) {
        User foundUser =
                userRepository.findByExternalId(userExternalId).orElseThrow(() -> new ResourceNotFoundException("User"
                        , "External Id", userExternalId));

        ItemGroup itemGroup = itemGroupMapper.toEntity(itemGroupCreateDto);
        itemGroup.setUser(foundUser);
        itemGroup.setId(UUID.randomUUID());

        return saveGroupChangesInDb(itemGroup);
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    @Override
    public void createDefaultGroup(User user) {
        ItemGroup defaultGroup = new ItemGroup();
        defaultGroup.setId(UUID.randomUUID());
        defaultGroup.setName("Ungrouped");
        defaultGroup.setDescription("Stash items that are not assigned to any group");
        defaultGroup.setDefaultGroup(true);
        defaultGroup.setUser(user);

        itemGroupRepository.save(defaultGroup);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ItemGroupDto update(UUID id, UUID userExternalId, ItemGroupUpdateDto itemGroupUpdateDto) {
        ItemGroup group = itemGroupRepository.findByIdAndUserExternalId(id, userExternalId).orElseThrow(
                () -> new ResourceNotFoundException("ItemGroup", "Id", id));
        ItemGroup updatedGroup = itemGroupMapper.updateFromDto(itemGroupUpdateDto, group);
        return saveGroupChangesInDb(updatedGroup);
    }

    private ItemGroupDto saveGroupChangesInDb(ItemGroup itemGroup) {
        try {
            ItemGroup savedGroup = itemGroupRepository.save(itemGroup);
            return itemGroupMapper.toDto(savedGroup);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("item_groups_user_id_name_unique")) {
                throw new ResourceAlreadyExistsException("ItemGroup", "Name", itemGroup.getName());
            }
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(UUID id, UUID userExternalId) {
        ItemGroup group = itemGroupRepository.findByIdAndUserExternalId(id, userExternalId).orElseThrow(
                () -> new ResourceNotFoundException("ItemGroup", "Id", id));
        itemGroupRepository.delete(group);
    }
}
