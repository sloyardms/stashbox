package com.sloyardms.stashbox.stashitem.mapper;

import com.sloyardms.stashbox.itemgroup.mapper.ItemGroupMapper;
import com.sloyardms.stashbox.itemimage.mapper.ItemImageMapper;
import com.sloyardms.stashbox.itemtag.mapper.ItemTagMapper;
import com.sloyardms.stashbox.stashitem.dto.CreateStashItemRequest;
import com.sloyardms.stashbox.stashitem.dto.StashItemResponse;
import com.sloyardms.stashbox.stashitem.dto.StashItemSummaryResponse;
import com.sloyardms.stashbox.stashitem.dto.UpdateStashItemRequest;
import com.sloyardms.stashbox.stashitem.entity.StashItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {ItemGroupMapper.class, ItemImageMapper.class, ItemTagMapper.class})
public interface StashItemMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "normalizedTitle", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "favorite", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    StashItem toEntity(CreateStashItemRequest createStashItemRequest);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "normalizedTitle", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    StashItem updateFromRequest(UpdateStashItemRequest updateStashItemRequest, @MappingTarget StashItem stashItem);

    StashItemResponse toResponse(StashItem stashItem);

    StashItemSummaryResponse toSummaryResponse(StashItem stashItem);

}
