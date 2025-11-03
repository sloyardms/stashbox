package com.sloyardms.stashbox.user_filter;

import com.sloyardms.stashbox.user_filter.entity.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserFilterRepository extends JpaRepository<UserFilter, UUID> {

    @Query(value = """
                SELECT uf
                FROM UserFilter uf
                WHERE uf.user.id = :userId
                      AND (uf.normalizedFilterName LIKE LOWER(CONCAT('%', :query, '%'))
                               OR uf.normalizedUrlPattern LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<UserFilter> findAllByUserAndQuery(UUID userId, String query, Pageable pageable);

}
