package com.sloyardms.backend.integration.user_filter;

import com.sloyardms.backend.user_filter.dto.UserFilterUpdateDto;

public class UserFilterUpdateDtoDataBuilder {

    private String filterName = "updated_filter";
    private String urlPattern = "http://www.updated.com/title";
    private String extractionRegex = "(?<updated>.*)";
    private Boolean active = null;

    private UserFilterUpdateDtoDataBuilder() {
    }

    public static UserFilterUpdateDtoDataBuilder aValidUpdateDto() {
        return new UserFilterUpdateDtoDataBuilder();

    }

    public static UserFilterUpdateDtoDataBuilder anEmptyUpdateDto() {
        return new UserFilterUpdateDtoDataBuilder()
                .withoutFilterName()
                .withoutUrlPattern()
                .withoutExtractionRegex();
    }

    UserFilterUpdateDtoDataBuilder withFilterName(String filterName) {
        this.filterName = filterName;
        return this;
    }

    UserFilterUpdateDtoDataBuilder withUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
        return this;
    }

    UserFilterUpdateDtoDataBuilder withExtractionRegex(String regex) {
        this.extractionRegex = regex;
        return this;
    }

    UserFilterUpdateDtoDataBuilder withoutFilterName() {
        this.filterName = null;
        return this;
    }

    UserFilterUpdateDtoDataBuilder withoutUrlPattern() {
        this.urlPattern = null;
        return this;
    }

    UserFilterUpdateDtoDataBuilder withoutExtractionRegex() {
        this.extractionRegex = null;
        return this;
    }

    UserFilterUpdateDtoDataBuilder asActive() {
        this.active = true;
        return this;
    }

    UserFilterUpdateDtoDataBuilder asInactive() {
        this.active = false;
        return this;
    }

    UserFilterUpdateDto build() {
        return UserFilterUpdateDto.builder()
                .filterName(filterName)
                .urlPattern(urlPattern)
                .extractionRegex(extractionRegex)
                .active(active)
                .build();
    }

}
