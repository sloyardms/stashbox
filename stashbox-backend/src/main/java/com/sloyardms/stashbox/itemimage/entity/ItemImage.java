package com.sloyardms.stashbox.itemimage.entity;

import com.sloyardms.stashbox.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@Table(name = "item_images", indexes = {
        @Index(name = "item_images_user_id_index", columnList = "user_id")
})
public class ItemImage {

    @Id
    @Column(columnDefinition = "UUID")
    @ToString.Include
    private UUID id;

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

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @ToString.Include
    private Instant createdAt;

}
