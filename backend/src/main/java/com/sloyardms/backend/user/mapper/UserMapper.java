package com.sloyardms.backend.user.mapper;

import com.sloyardms.backend.user.dto.UserDetailDto;
import com.sloyardms.backend.user.dto.UserSummaryDto;
import com.sloyardms.backend.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDetailDto toDetailDto(User user);

    UserSummaryDto toSummaryDto(User user);

}
