package com.sloyardms.stashbox.user.entity;

import com.sloyardms.stashbox.common.entity.Auditable;
import com.sloyardms.stashbox.userfilter.entity.UserFilter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "users_external_id_unique", columnNames = "external_id"),
                @UniqueConstraint(name = "users_username_unique", columnNames = "username"),
                @UniqueConstraint(name = "users_email_unique", columnNames = "email")
        })
public class User extends Auditable {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    @ToString.Include
    private UUID id;

    @Column(name = "external_id", unique = true, nullable = false)
    @ToString.Include
    private UUID externalId;

    @Column(name = "username", unique = true, nullable = false)
    @ToString.Include
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    @ToString.Include
    private String email;

    @Column(name = "settings", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    private UserSettings settings = new UserSettings();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<UserFilter> filters = new ArrayList<>();

}
