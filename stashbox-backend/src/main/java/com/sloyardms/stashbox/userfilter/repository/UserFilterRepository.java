package com.sloyardms.stashbox.userfilter.repository;

import com.sloyardms.stashbox.common.repository.UserScopedRepository;
import com.sloyardms.stashbox.userfilter.entity.UserFilter;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserFilterRepository extends UserScopedRepository<UserFilter, UUID>,
        JpaSpecificationExecutor<UserFilter> {

    @Modifying
    @Query("""
            UPDATE UserFilter uf
            SET uf.matchCount = uf.matchCount + 1, uf.lastMatchedAt = :matchedAt
            WHERE uf.id = :id AND uf.user.externalId = :userExternalId
            """)
    int incrementMatchCount(@Param("id") UUID id, @Param("userExternalId") UUID userExternalId,
                            @Param("matchedAt") Instant matchedAt);

    @Query("""
            SELECT DISTINCT uf.domain
            FROM UserFilter uf
            WHERE uf.user.externalId = :userExternalId
            ORDER BY uf.domain
            """)
    List<String> findDistinctDomainsByUserExternalId(@Param("userExternalId") UUID userExternalId);

}
