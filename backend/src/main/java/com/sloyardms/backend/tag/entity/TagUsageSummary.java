package com.sloyardms.backend.tag.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
@Table(name = "tag_usage_summary")
@IdClass(TagUsageSummaryId.class)
public class TagUsageSummary {

    @Id
    @Column(name = "user_uuid", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "group_uuid", nullable = false)
    private UUID groupId;

    @Id
    @Column(name = "tag_uuid", nullable = false)
    private UUID tagId;

    @Column(name = "bookmark_count", nullable = false)
    @Min(0)
    private int itemCount;

}
