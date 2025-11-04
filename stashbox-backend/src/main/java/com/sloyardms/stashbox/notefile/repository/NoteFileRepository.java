package com.sloyardms.stashbox.notefile.repository;

import com.sloyardms.stashbox.notefile.entity.NoteFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NoteFileRepository extends JpaRepository<NoteFile, UUID> {
}
