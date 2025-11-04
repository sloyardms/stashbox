package com.sloyardms.stashbox.itemimage.repository;

import com.sloyardms.stashbox.itemimage.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemImageRepository extends JpaRepository<ItemImage, UUID> {
}
