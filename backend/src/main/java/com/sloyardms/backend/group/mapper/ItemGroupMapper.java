package com.sloyardms.backend.group.mapper;

import com.sloyardms.backend.group.dto.ItemGroupCreateDto;
import com.sloyardms.backend.group.dto.ItemGroupDto;
import com.sloyardms.backend.group.dto.ItemGroupUpdateDto;
import com.sloyardms.backend.group.entity.ItemGroup;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ItemGroupMapper {

    ItemGroupDto toDto(ItemGroup itemGroup);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    ItemGroup toEntity(ItemGroupCreateDto itemGroupCreateDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ItemGroup updateFromDto(ItemGroupUpdateDto itemGroupUpdateDto, @MappingTarget ItemGroup itemGroup);

}
