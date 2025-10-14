package com.sloyardms.backend.user.entity;

import com.sloyardms.backend.common.entity.Auditable;
import com.sloyardms.backend.user_filter.entity.UserFilter;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends Auditable {

    public User() {
        this.settings = UserSettings.withDefaults(null);
    }

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "external_id", unique = true, nullable = false)
    private UUID externalId;

    @Column(name = "username", unique = true, nullable = false)
    private String userName;

    @Type(JsonType.class)
    @Column(name = "settings", columnDefinition = "jsonb", nullable = false)
    private UserSettings settings;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<UserFilter> filters = new ArrayList<>();

    @PostLoad
    @PostUpdate
    private void ensureDefaultSettings() {
        this.settings = UserSettings.withDefaults(this.settings);
    }

}
