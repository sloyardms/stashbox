package com.sloyardms.stashbox.itemgroup.mapper;

import com.sloyardms.stashbox.itemgroup.dto.CreateItemGroupRequest;
import com.sloyardms.stashbox.itemgroup.dto.ItemGroupResponse;
import com.sloyardms.stashbox.itemgroup.dto.UpdateItemGroupRequest;
import com.sloyardms.stashbox.itemgroup.entity.ItemGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ItemGroupMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "normalizedName", ignore = true)
    @Mapping(target = "id", ignore = true)
    ItemGroup toEntity(CreateItemGroupRequest createItemGroupRequest);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "normalizedName", ignore = true)
    @Mapping(target = "id", ignore = true)
    ItemGroup updateFromRequest(UpdateItemGroupRequest updateItemGroupRequest, @MappingTarget ItemGroup itemGroup);

    ItemGroupResponse toResponse(ItemGroup itemGroup);

}
