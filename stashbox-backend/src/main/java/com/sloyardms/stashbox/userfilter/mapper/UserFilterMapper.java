package com.sloyardms.stashbox.userfilter.mapper;

import com.sloyardms.stashbox.userfilter.dto.CreateUserFilterRequest;
import com.sloyardms.stashbox.userfilter.dto.UpdateUserFilterRequest;
import com.sloyardms.stashbox.userfilter.dto.UserFilterResponse;
import com.sloyardms.stashbox.userfilter.entity.UserFilter;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserFilterMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "normalizedUrlPattern", ignore = true)
    @Mapping(target = "normalizedFilterName", ignore = true)
    @Mapping(target = "matchCount", ignore = true)
    @Mapping(target = "lastMatchedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserFilter toEntity(CreateUserFilterRequest createUserFilterRequest);

    UserFilterResponse toResponse(UserFilter userFilter);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "normalizedUrlPattern", ignore = true)
    @Mapping(target = "normalizedFilterName", ignore = true)
    @Mapping(target = "matchCount", ignore = true)
    @Mapping(target = "lastMatchedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserFilter updateFromRequest(UpdateUserFilterRequest updateUserFilterRequest, @MappingTarget UserFilter userFilter);

}
