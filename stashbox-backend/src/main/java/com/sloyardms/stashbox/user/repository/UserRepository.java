package com.sloyardms.stashbox.user.repository;

import com.sloyardms.stashbox.user.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Cacheable(value = "userIdByExternalId", key = "#externalId")
    @Query("SELECT u.id FROM User u WHERE u.externalId = :externalId")
    Optional<UUID> findIdByExternalId(@Param("externalId") UUID externalId);

    Optional<User> findByExternalId(UUID externalId);

    Long deleteByExternalId(UUID externalId);

}
