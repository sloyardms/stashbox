package com.sloyardms.backend.user;

import com.sloyardms.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByExternalId(UUID externalId);

    Long deleteByExternalId(UUID externalId);

}
