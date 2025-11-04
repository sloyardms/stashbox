package com.sloyardms.stashbox.stashitem.entity;

import com.sloyardms.stashbox.common.entity.Auditable;
import com.sloyardms.stashbox.itemgroup.entity.ItemGroup;
import com.sloyardms.stashbox.itemimage.entity.ItemImage;
import com.sloyardms.stashbox.itemtag.entity.ItemTag;
import com.sloyardms.stashbox.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
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
    private boolean favorite = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", unique = true)
    private ItemImage image;

    @Column(name = "deleted_at")
    @ToString.Include
    private OffsetDateTime deletedAt;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "item_tags",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<ItemTag> tags = new ArrayList<>();

}
