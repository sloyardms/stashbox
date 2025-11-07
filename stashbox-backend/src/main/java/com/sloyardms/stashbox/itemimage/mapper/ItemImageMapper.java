package com.sloyardms.stashbox.itemimage.mapper;

import com.sloyardms.stashbox.itemimage.dto.ItemImageResponse;
import com.sloyardms.stashbox.itemimage.entity.ItemImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemImageMapper {

    @Mapping(target = "path", source = "filePath")
    ItemImageResponse toResponse(ItemImage itemImage);

}
