package com.sloyardms.backend.integration.user_filter;

import com.sloyardms.backend.user.entity.User;
import com.sloyardms.backend.user_filter.UserFilterRepository;
import com.sloyardms.backend.user_filter.entity.UserFilter;

import java.util.UUID;

public class UserFilterEntityDataBuilder {

    private UUID id = UUID.randomUUID();
    private UUID userId;
    private String filterName = "test_filter";
    private String urlPattern = "http://www.test.com/title";
    private String extractionRegex = "(?<title>.*)";

    private UserFilterEntityDataBuilder() {
    }

    public static UserFilterEntityDataBuilder aValidFilter(UUID userId) {
        UserFilterEntityDataBuilder builder = new UserFilterEntityDataBuilder();
        builder.userId = userId;
        return builder;
    }

    UserFilterEntityDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    UserFilterEntityDataBuilder withFilterName(String filterName) {
        this.filterName = filterName;
        return this;
    }

    UserFilterEntityDataBuilder withUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
        return this;
    }

    UserFilterEntityDataBuilder withExtractionRegex(String regex) {
        this.extractionRegex = regex;
        return this;
    }

    UserFilter build() {
        return UserFilter.builder()
                .id(id)
                .user(User.builder().id(userId).build())
                .filterName(filterName)
                .urlPattern(urlPattern)
                .extractionRegex(extractionRegex)
                .active(true)
                .build();
    }

    UserFilter buildAndSave(UserFilterRepository repository) {
        return repository.save(build());
    }

}
