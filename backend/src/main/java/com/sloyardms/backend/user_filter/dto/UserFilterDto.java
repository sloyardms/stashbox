package com.sloyardms.backend.user_filter.dto;

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
public class UserFilterDto {

    private UUID id;
    private String filterName;
    private String urlPattern;
    private String extractionRegex;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

}
