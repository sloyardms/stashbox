package com.sloyardms.backend.user_filter.entity;

import com.sloyardms.backend.common.entity.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_filters", uniqueConstraints = {
                @UniqueConstraint(name = "user_filter_url_pattern_unique", columnNames = {"user_id", "url_pattern"})
})
public class UserFilter extends Auditable {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "filter_name", nullable = false)
    private String filterName;

    @Column(name = "url_pattern", nullable = false)
    private String urlPattern;

    @Column(name = "extraction_regex", nullable = false)
    private String extractionRegex;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

}
