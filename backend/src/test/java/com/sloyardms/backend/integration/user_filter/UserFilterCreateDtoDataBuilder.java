package com.sloyardms.backend.integration.user_filter;

import com.sloyardms.backend.user_filter.dto.UserFilterCreateDto;

public class UserFilterCreateDtoDataBuilder {

    private String filterName = "test_filter";
    private String urlPattern = "http://www.test.com/title";
    private String extractionRegex = "(?<title>.*)";

    private UserFilterCreateDtoDataBuilder() {
    }

    public static UserFilterCreateDtoDataBuilder aValidCreateDto() {
        return new UserFilterCreateDtoDataBuilder();
    }

    public static UserFilterCreateDtoDataBuilder anInvalidCreateDto() {
        return new UserFilterCreateDtoDataBuilder()
                .withoutFilterName()
                .withoutUrlPattern()
                .withoutExtractionRegex();
    }

    UserFilterCreateDtoDataBuilder withFilterName(String filterName) {
        this.filterName = filterName;
        return this;
    }

    UserFilterCreateDtoDataBuilder withUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
        return this;
    }

    UserFilterCreateDtoDataBuilder withExtractionRegex(String regex) {
        this.extractionRegex = regex;
        return this;
    }

    UserFilterCreateDtoDataBuilder withoutFilterName() {
        this.filterName = null;
        return this;
    }

    UserFilterCreateDtoDataBuilder withoutUrlPattern() {
        this.urlPattern = null;
        return this;
    }

    UserFilterCreateDtoDataBuilder withoutExtractionRegex() {
        this.extractionRegex = null;
        return this;
    }

    UserFilterCreateDto build() {
        return UserFilterCreateDto.builder()
                .filterName(filterName)
                .urlPattern(urlPattern)
                .extractionRegex(extractionRegex)
                .build();
    }

}
