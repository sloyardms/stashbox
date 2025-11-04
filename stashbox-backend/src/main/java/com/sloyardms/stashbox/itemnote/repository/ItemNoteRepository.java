package com.sloyardms.stashbox.itemnote.repository;

import com.sloyardms.stashbox.itemnote.entity.ItemNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemNoteRepository extends JpaRepository<ItemNote, UUID> {
}
