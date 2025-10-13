package com.sloyardms.backend.group;

import com.sloyardms.backend.group.entity.ItemGroup;
import com.sloyardms.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ItemGroupRepository extends JpaRepository<ItemGroup, UUID> {

    @Query("""
                 SELECT g
                 FROM ItemGroup g
                 INNER JOIN User u ON g.user.id = u.id 
                 WHERE g.id = :groupId AND u.externalId = :externalId
            """)
    Optional<ItemGroup> findByIdAndUserExternalId(
            @Param("groupId") UUID groupId,
            @Param("externalId") UUID userExternalId
    );

    @Query("""
                SELECT g FROM ItemGroup g
                INNER JOIN User u ON g.user.id = u.id 
                WHERE u.externalId = :externalId
            """)
    Page<ItemGroup> findAllByUser(
            @Param("externalId") UUID userExternalId,
            Pageable pageable);

}
