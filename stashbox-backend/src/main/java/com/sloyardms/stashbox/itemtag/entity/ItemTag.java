package com.sloyardms.stashbox.itemtag.entity;

import com.sloyardms.stashbox.common.entity.Auditable;
import com.sloyardms.stashbox.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "tags",
        indexes = {
                @Index(name = "tags_user_id_index", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "tags_name_unique", columnNames = {"user_id",
                        "normalized_name"}),
                @UniqueConstraint(name = "tags_slug_unique", columnNames = {"user_id",
                        "slug"})
        }
)
public class ItemTag extends Auditable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 50)
    @ToString.Include
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 50)
    @ToString.Include
    private String normalizedName;

    @Column(name = "slug", nullable = false, length = 75)
    @ToString.Include
    private String slug;

}
