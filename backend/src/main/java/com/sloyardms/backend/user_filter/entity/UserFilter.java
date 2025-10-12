package com.sloyardms.backend.user_filter.entity;

import com.sloyardms.backend.common.entity.Auditable;
import com.sloyardms.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "filter_name", nullable = false)
    private String filterName;

    @Column(name = "url_pattern", nullable = false)
    private String urlPattern;

    @Column(name = "extraction_regex", nullable = false)
    private String extractionRegex;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

}
