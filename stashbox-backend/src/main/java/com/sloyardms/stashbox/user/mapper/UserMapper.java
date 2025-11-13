package com.sloyardms.stashbox.user.mapper;

import com.sloyardms.stashbox.user.dto.AdminUserResponse;
import com.sloyardms.stashbox.user.dto.UserResponse;
import com.sloyardms.stashbox.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserSettingsMapper.class})
public interface UserMapper {

    UserResponse toResponse(User user);

    AdminUserResponse toAdminResponse(User user);

}
