package com.sloyardms.backend.user.entity;

import com.sloyardms.backend.common.entity.Auditable;
import com.sloyardms.backend.group.entity.ItemGroup;
import com.sloyardms.backend.stash_item.StashItem;
import com.sloyardms.backend.tag.entity.Tag;
import com.sloyardms.backend.user_filter.entity.UserFilter;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends Auditable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "external_id", unique = true, nullable = false)
    private UUID externalId;

    @Type(JsonType.class)
    @Column(name = "settings", columnDefinition = "jsonb", nullable = false)
    private UserSettings settings;

    @PostLoad
    private void ensureDefaultSettings(){
        this.settings = UserSettings.withDefaults(this.settings);
    }

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<UserFilter> filters;

}
