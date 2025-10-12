package com.sloyardms.backend.user_filter.service;

import com.sloyardms.backend.user_filter.dto.UserFilterCreateDto;
import com.sloyardms.backend.user_filter.dto.UserFilterDto;
import com.sloyardms.backend.user_filter.dto.UserFilterUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IUserFilterService {

    UserFilterDto getById(UUID id, UUID userExternalId);

    Page<UserFilterDto> getUserFilters(UUID userExternalId, Pageable pageable);

    UserFilterDto create(UUID userExternalId, UserFilterCreateDto userFilterCreateDto);

    UserFilterDto update(UUID id, UUID userExternalId, UserFilterUpdateDto userFilterUpdateDto);

    void delete(UUID id, UUID userExternalId);

}
