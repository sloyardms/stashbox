package com.sloyardms.backend.user_filter;

import com.sloyardms.backend.user_filter.entity.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserFilterRepository extends JpaRepository<UserFilter, UUID> {

    @Query("""
                SELECT f
                FROM UserFilter f
                INNER JOIN User u ON f.user.id = u.id
                WHERE f.id = :filterId AND u.externalId = :externalId
            """)
    Optional<UserFilter> findByIdAndUserExternalId(
            @Param("filterId") UUID filterId,
            @Param("externalId") UUID userExternalId);

    @Query("""
                SELECT f FROM UserFilter f
                INNER JOIN User u ON f.user.id = u.id
                WHERE u.externalId = :externalId
            """)
    Page<UserFilter> findByUser(
            @Param("externalId") UUID userExternalId,
            Pageable pageable);

}
