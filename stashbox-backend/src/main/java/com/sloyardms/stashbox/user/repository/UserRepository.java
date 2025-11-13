package com.sloyardms.stashbox.user.repository;

import com.sloyardms.stashbox.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByExternalId(UUID externalId);

    Page<User> findByExternalIdNot(UUID externalId, Pageable pageable);

}
