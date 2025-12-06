package com.sloyardms.stashbox.userfilter.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserFilterResponse {

    private UUID id;
    private String filterName;
    private String description;
    private String urlPattern;
    private String domain;
    private String extractionRegex;
    private Integer captureGroupIndex;
    private Integer priority;
    private Boolean active;
    private Long matchCount;
    private Instant lastMatchedAt;
    private Instant createdAt;
    private Instant updatedAt;

}
