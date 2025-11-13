package com.sloyardms.stashbox.userfilter.dto;

import com.sloyardms.stashbox.common.annotations.AtLeastOneNonNullField;
import com.sloyardms.stashbox.common.annotations.ValidRegex;
import jakarta.validation.constraints.Max;
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
@AtLeastOneNonNullField
public class UpdateUserFilterRequest {

    @Size(max = 100, message = "{userFilter.filterName.maxSize}")
    private String filterName;

    @Size(max = 500, message = "{userFilter.description.maxSize}")
    private String description;

    @Size(max = 2048, message = "{userFilter.urlPattern.maxSize}")
    private String urlPattern;

    @Size(max = 255, message = "{userFilter.domain.maxSize}")
    private String domain;

    @Size(max = 1000, message = "{userFilter.extractionRegex.maxSize}")
    @ValidRegex(message = "{userFilter.extractionRegex.invalid}")
    private String extractionRegex;

    @PositiveOrZero(message = "{userFilter.captureGroupIndex.positiveOrZero}")
    @Max(value = 20, message = "{userFilter.captureGroupIndex.max}")
    private Integer captureGroupIndex;

    @PositiveOrZero(message = "{userFilter.priority.positiveOrZero}")
    private Integer priority;

    private Boolean active;

}
