package com.sloyardms.stashbox.user.mapper;

import com.sloyardms.stashbox.user.dto.UpdateUserSettingsRequest;
import com.sloyardms.stashbox.user.dto.UserSettingsResponse;
import com.sloyardms.stashbox.user.entity.UserSettings;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserSettingsMapper {

    UserSettingsResponse toResponse(UserSettings userSettings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserSettings updateFromDto(UpdateUserSettingsRequest updateUserSettingsRequest, @MappingTarget UserSettings userSettings);

}
