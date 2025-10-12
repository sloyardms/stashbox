package com.sloyardms.backend.user.service;

import com.sloyardms.backend.user.dto.UserDetailDto;
import com.sloyardms.backend.user.dto.UserSettingsUpdateDto;
import com.sloyardms.backend.user.dto.UserSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IUserService {

    UserDetailDto getById(UUID id);

    UserDetailDto getByExternalId(UUID externalId);

    Page<UserSummaryDto> getAllUsers(Pageable pageable);

    UserDetailDto create(UUID externalId, String username);

    void delete(UUID id);

    void deleteByExternalId(UUID externalId);

    UserDetailDto updateSettings(UUID externalId, UserSettingsUpdateDto userSettingsUpdateDto);

    UserDetailDto getDevUser();

}
