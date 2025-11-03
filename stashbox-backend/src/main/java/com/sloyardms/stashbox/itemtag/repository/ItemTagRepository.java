package com.sloyardms.stashbox.itemtag.repository;

import com.sloyardms.stashbox.itemtag.entity.ItemTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemTagRepository extends JpaRepository<ItemTag, UUID> {

    Optional<ItemTag> findByIdAndUserId(UUID id, UUID userId);

    Page<ItemTag> findAllByUserId(UUID userId, Pageable pageable);

    List<ItemTag> findAllByUserIdAndNameIn(UUID userId, Collection<String> names);

}
