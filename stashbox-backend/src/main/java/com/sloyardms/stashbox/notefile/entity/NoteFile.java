package com.sloyardms.stashbox.notefile.entity;

import com.sloyardms.stashbox.itemnote.entity.ItemNote;
import com.sloyardms.stashbox.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "note_files", indexes = {
        @Index(name = "note_files_note_order_unique", columnList = "note_id, display_order")
})
public class NoteFile {

    @Id
    @Column(name = "id")
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private ItemNote item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_filename", nullable = false, length = 255)
    @ToString.Include
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, length = 255)
    @ToString.Include
    private String storedFilename;

    @Column(name = "file_path", nullable = false, length = 500)
    @ToString.Include
    private String filePath;

    @Column(name = "mime_type", nullable = false, length = 127)
    @ToString.Include
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    @ToString.Include
    private Long fileSize;

    @Column(name = "file_extension", nullable = false, length = 10)
    @ToString.Include
    private String fileExtension;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false)
    private UploadStatus uploadStatus = UploadStatus.PENDING;

    @Column(name = "display_order", nullable = false)
    @ToString.Include
    private Integer displayOrder;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @ToString.Include
    private Instant createdAt;

}
