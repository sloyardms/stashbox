package com.sloyardms.stashbox.itemgroup.repository;

import com.sloyardms.stashbox.itemgroup.entity.ItemGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemGroupRepository extends JpaRepository<ItemGroup, UUID> {

    Page<ItemGroup> findByUserId(UUID userId, Pageable pageable);

}
