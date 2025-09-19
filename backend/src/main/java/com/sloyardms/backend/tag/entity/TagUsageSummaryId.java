package com.sloyardms.backend.tag.entity;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TagUsageSummaryId {

    private UUID userId;
    private UUID groupId;
    private UUID tagId;

}
