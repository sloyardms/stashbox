package com.sloyardms.stashbox.stashitem.entity;

import com.sloyardms.stashbox.common.entity.Auditable;
import com.sloyardms.stashbox.itemgroup.entity.ItemGroup;
import com.sloyardms.stashbox.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "stash_items", indexes = {
        @Index(name = "stash_items_user_created_active_index", columnList = "user_id, created_at"),
        @Index(name = "stash_items_user_group_created_active_index", columnList = "user_id, group_id, created_at"),
        @Index(name = "stash_items_favorites_by_group_index", columnList = "user_id, group_id, created_at, is_favorite"),
})
public class StashItem extends Auditable {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private ItemGroup group;

    @Column(name = "title", length = 255)
    @ToString.Include
    private String title;

    @Column(name = "normalized_title", length = 255)
    @ToString.Include
    private String normalizedTitle;

    @Column(name = "slug", length = 300)
    @ToString.Include
    private String slug;

    @Column(name = "url", length = 1000)
    @ToString.Include
    private String url;

    @Column(name = "description", length = 500)
    @ToString.Include
    private String description;

    @Column(name = "is_favorite", nullable = false)
    @ToString.Include
    private Boolean favorite = false;

    @Column(name = "image_id", columnDefinition = "UUID")
    private UUID imageId;

    @Column(name = "deleted_at")
    @ToString.Include
    private OffsetDateTime deletedAt;

}
