package com.sloyardms.backend.user_filter.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterUpdateDto {

    @Size(max = 100, message = "{userFilter.filterName.maxSize}")
    private String filterName;

    @Size(max = 500, message = "{userFilter.urlPattern.maxSize}")
    private String urlPattern;

    @Size(max = 1000, message = "{userFilter.extractionRegex.maxSize}")
    private String extractionRegex;

    private Boolean active;

    @AssertTrue(message = "{userFilter.update.atLeastOneFieldProvided}")
    public boolean hasAtLeastOneField() {
        return filterName != null ||
                urlPattern != null ||
                extractionRegex != null ||
                active != null;
    }
}
