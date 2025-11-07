package com.sloyardms.stashbox.userfilter.dto;

import com.sloyardms.stashbox.common.annotations.ValidRegex;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CreateUserFilterRequest {

    @NotBlank(message = "{userFilter.filterName.notBlank}")
    @Size(max = 100, message = "{userFilter.filterName.maxSize}")
    private String filterName;

    @Size(max = 500, message = "{userFilter.description.maxSize}")
    private String description;

    @NotBlank(message = "{userFilter.urlPattern.notBlank}")
    @Size(max = 2048, message = "{userFilter.urlPattern.maxSize}")
    private String urlPattern;

    @NotBlank(message = "{userFilter.domainFilter.notBlank}")
    @Size(max = 255, message = "{userFilter.domainFilter.maxSize}")
    private String domainFilter;

    @NotBlank(message = "{userFilter.extractionRegex.notBlank}")
    @Size(max = 1000, message = "{userFilter.extractionRegex.maxSize}")
    @ValidRegex(message = "{userFilter.extractionRegex.invalid}")
    private String extractionRegex;

    @NotNull(message = "{userFilter.captureGroupIndex.notNull}")
    @PositiveOrZero(message = "{userFilter.captureGroupIndex.positiveOrZero}")
    @Max(value = 20, message = "{userFilter.captureGroupIndex.max}")
    private Integer captureGroupIndex;

    @NotNull(message = "{userFilter.priority.notNull}")
    @PositiveOrZero(message = "{userFilter.priority.positiveOrZero}")
    private Integer priority;

}
