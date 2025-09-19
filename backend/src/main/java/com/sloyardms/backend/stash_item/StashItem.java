package com.sloyardms.backend.stash_item;

import com.sloyardms.backend.common.entity.Auditable;
import com.sloyardms.backend.group.entity.ItemGroup;
import com.sloyardms.backend.item_image.entity.ItemImage;
import com.sloyardms.backend.note.entity.ItemNote;
import com.sloyardms.backend.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stash_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "stash_items_user_id_title_unique", columnNames = {"user_id", "title"}),
                @UniqueConstraint(name = "stash_items_user_id_url_unique", columnNames = {"user_id", "url"})
        },
        indexes = {
                @Index(name = "stash_items_user_id_index", columnList = "user_id"),
                @Index(name = "stash_items_group_id_index", columnList = "group_id")
        }
)
public class StashItem extends Auditable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ItemGroup group;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "description")
    private String description;

    @Column(name = "is_favorite", nullable = false)
    private boolean isFavorite = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "item_tags",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private List<ItemImage> images = new ArrayList<>();

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private List<ItemNote> notes = new ArrayList<>();


}
