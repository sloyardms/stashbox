package com.sloyardms.stashbox.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface UserScopedRepository<T, ID> extends JpaRepository<T, ID> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.user.externalId = :userExternalId")
    Optional<T> findByIdAndUserExternalId(@Param("id") ID id, @Param("userExternalId") UUID userExternalId);

}
