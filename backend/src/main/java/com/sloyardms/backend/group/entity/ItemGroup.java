package com.sloyardms.backend.group.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sloyardms.backend.common.entity.Auditable;
import com.sloyardms.backend.stash_item.StashItem;
import com.sloyardms.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "item_groups",
        uniqueConstraints = {
            @UniqueConstraint(name = "item_groups_user_id_name_unique", columnNames = {"user_id", "name"})
        },
        indexes = {
            @Index(name = "item_groups_user_id_index" , columnList = "user_id")
        })
public class ItemGroup extends Auditable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

}
