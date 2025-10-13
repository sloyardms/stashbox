package com.sloyardms.backend.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemGroupDto {

    private UUID id;
    private String name;
    private String description;
    private boolean defaultGroup;
    private Instant createdAt;
    private Instant updatedAt;

}
