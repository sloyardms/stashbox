package com.sloyardms.stashbox.userfilter.entity;

import com.sloyardms.stashbox.common.entity.Auditable;
import com.sloyardms.stashbox.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "user_filters",
        indexes = {
                @Index(name = "user_filters_user_id", columnList = "user_id"),
                @Index(name = "user_filters_user_id_active", columnList = "user_id, active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "user_filters_normalized_filter_name_unique", columnNames = {"user_id",
                        "normalized_filter_name"}),
                @UniqueConstraint(name = "user_filters_normalized_url_pattern_unique", columnNames = {"user_id",
                        "normalized_url_pattern"})
        }
)
public class UserFilter extends Auditable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "filter_name", nullable = false, length = 100)
    private String filterName;

    @Column(name = "normalized_filter_name", nullable = false, length = 100)
    @ToString.Include
    private String normalizedFilterName;

    @Column(name = "description", length = 500)
    @ToString.Include
    private String description;

    @Column(name = "url_pattern", nullable = false, length = 2048)
    private String urlPattern;

    @Column(name = "normalized_url_pattern", nullable = false, length = 2048)
    @ToString.Include
    private String normalizedUrlPattern;

    @Column(name = "domain_filter", length = 255)
    @ToString.Include
    private String domainFilter;

    @Column(name = "extraction_regex", length = 1000)
    @ToString.Include
    private String extractionRegex;

    @Column(name = "capture_group_index", nullable = false)
    @ToString.Include
    private Integer captureGroupIndex = 1;

    @Column(name = "priority", nullable = false)
    @ToString.Include
    private Integer priority = 0;

    @Column(name = "is_active", nullable = false)
    @ToString.Include
    private Boolean active = true;

    @Column(name = "match_count", nullable = false)
    @ToString.Include
    private Long matchCount = 0L;

    @Column(name = "last_matched_at")
    @ToString.Include
    private Instant lastMatchedAt;

}
