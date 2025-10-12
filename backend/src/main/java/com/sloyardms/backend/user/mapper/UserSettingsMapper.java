package com.sloyardms.backend.user.mapper;

import com.sloyardms.backend.user.dto.UserSettingsDto;
import com.sloyardms.backend.user.dto.UserSettingsUpdateDto;
import com.sloyardms.backend.user.entity.UserSettings;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserSettingsMapper {

    UserSettingsDto toDto(UserSettings userSettings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserSettings updateFromDto(UserSettingsUpdateDto userSettingsUpdateDto, @MappingTarget UserSettings userSettings);

}
