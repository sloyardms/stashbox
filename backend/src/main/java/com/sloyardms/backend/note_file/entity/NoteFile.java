package com.sloyardms.backend.note_file.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sloyardms.backend.note.entity.ItemNote;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "note_files",
        indexes = {
            @Index(name = "note_files_note_id_index", columnList = "note_id")
        }
)
public class NoteFile {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "note_id", nullable = false)
    private UUID noteId;

    @Column(name = "original_filename", nullable = false)
    private String originalFileName;

    @Column(name = "stored_filename", nullable = false)
    private String storedFileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "file_extension", nullable = false)
    private String fileExtension;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }

}
