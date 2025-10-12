package com.sloyardms.backend.user_filter.dto;

import jakarta.validation.constraints.NotBlank;
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
public class UserFilterCreateDto {

    @NotBlank(message = "{userFilter.filterName.notBlank}")
    @Size(max = 100, message = "{userFilter.filterName.maxSize}")
    private String filterName;

    @NotBlank(message = "{userFilter.urlPattern.notBlank}")
    @Size(max = 500, message = "{userFilter.urlPattern.maxSize}")
    private String urlPattern;

    @NotBlank(message = "{userFilter.extractionRegex.notBlank}")
    @Size(max = 1000, message = "{userFilter.extractionRegex.maxSize}")
    private String extractionRegex;

}
