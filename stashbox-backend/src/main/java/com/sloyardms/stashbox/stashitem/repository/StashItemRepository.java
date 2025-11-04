package com.sloyardms.stashbox.stashitem.repository;

import com.sloyardms.stashbox.stashitem.entity.StashItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StashItemRepository extends JpaRepository<StashItem, UUID> {
}
