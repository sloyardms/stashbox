package com.sloyardms.stashbox.itemtag.mapper;

import com.sloyardms.stashbox.itemtag.dto.CreateItemTagRequest;
import com.sloyardms.stashbox.itemtag.dto.ItemTagResponse;
import com.sloyardms.stashbox.itemtag.dto.ItemTagSummaryResponse;
import com.sloyardms.stashbox.itemtag.dto.UpdateItemTagRequest;
import com.sloyardms.stashbox.itemtag.entity.ItemTag;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ItemTagMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "normalizedName", ignore = true)
    @Mapping(target = "id", ignore = true)
    ItemTag toEntity(CreateItemTagRequest createItemTagRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "normalizedName", ignore = true)
    @Mapping(target = "id", ignore = true)
    ItemTag updateFromRequest(UpdateItemTagRequest updateItemTagRequest, @MappingTarget ItemTag itemTag);

    ItemTagResponse toResponse(ItemTag itemTag);

    ItemTagSummaryResponse toSummaryResponse(ItemTag itemTag);

}
