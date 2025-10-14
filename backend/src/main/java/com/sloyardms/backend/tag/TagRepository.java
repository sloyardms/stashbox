package com.sloyardms.backend.tag;

import com.sloyardms.backend.tag.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    @Query("""
                 SELECT t
                 FROM Tag t
                 INNER JOIN User u ON t.user.id = u.id
                 WHERE t.id = :tagId AND u.externalId = :externalId
            """)
    Optional<Tag> findByIdAndUserExternalId(
            @Param("tagId") UUID tagId,
            @Param("externalId") UUID userExternalId);

    @Query("""
                SELECT t FROM Tag t
                INNER JOIN User u ON t.user.id = u.id
                WHERE u.externalId = :externalId
            """)
    Page<Tag> findAllByUser(
            @Param("externalId") UUID userExternalId,
            Pageable pageable);

}
