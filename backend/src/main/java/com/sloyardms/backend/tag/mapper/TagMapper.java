package com.sloyardms.backend.tag.mapper;

import com.sloyardms.backend.tag.dto.TagCreateDto;
import com.sloyardms.backend.tag.dto.TagDto;
import com.sloyardms.backend.tag.dto.TagUpdateDto;
import com.sloyardms.backend.tag.entity.Tag;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagDto toDto(Tag tag);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    Tag toEntity(TagCreateDto tagCreateDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tag updateFromDto(TagUpdateDto tagUpdateDto, @MappingTarget Tag tag);

}
