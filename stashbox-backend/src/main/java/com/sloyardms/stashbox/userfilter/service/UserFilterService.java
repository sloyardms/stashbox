package com.sloyardms.stashbox.userfilter.service;

import com.sloyardms.stashbox.userfilter.dto.CreateUserFilterRequest;
import com.sloyardms.stashbox.userfilter.dto.UpdateUserFilterRequest;
import com.sloyardms.stashbox.userfilter.dto.UserFilterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserFilterService {

    UserFilterResponse getUserFilterById(UUID id, UUID userExternalId);

    Page<UserFilterResponse> getAllUserFilters(String searchQuery, Boolean active, String domain, UUID userExternalId,
                                               Pageable pageable);

    UserFilterResponse saveUserFilter(CreateUserFilterRequest request, UUID userExternalId);

    UserFilterResponse updateUserFilter(UUID id, UpdateUserFilterRequest request, UUID userExternalId);

    void deleteUserFilter(UUID id, UUID userExternalId);

    void recordFilterMatch(UUID id, UUID userExternalId);

    List<String> getUserFilterDomains(UUID userExternalId);

}
