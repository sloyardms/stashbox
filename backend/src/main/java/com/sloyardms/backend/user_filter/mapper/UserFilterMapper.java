package com.sloyardms.backend.user_filter.mapper;

import com.sloyardms.backend.user_filter.dto.UserFilterCreateDto;
import com.sloyardms.backend.user_filter.dto.UserFilterDto;
import com.sloyardms.backend.user_filter.dto.UserFilterUpdateDto;
import com.sloyardms.backend.user_filter.entity.UserFilter;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserFilterMapper {

    UserFilterDto toDto(UserFilter userFilter);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "filterName", source = "filterName")
    @Mapping(target = "urlPattern", source = "urlPattern")
    @Mapping(target = "extractionRegex", source = "extractionRegex")
    UserFilter toEntity(UserFilterCreateDto userFilterCreateDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserFilter updateFromDto(UserFilterUpdateDto userFilterUpdateDto, @MappingTarget UserFilter userFilter);

}
