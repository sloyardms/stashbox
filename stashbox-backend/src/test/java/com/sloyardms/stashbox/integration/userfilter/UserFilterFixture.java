package com.sloyardms.stashbox.integration.userfilter;

import com.sloyardms.stashbox.user.entity.User;
import com.sloyardms.stashbox.userfilter.entity.UserFilter;
import com.sloyardms.stashbox.userfilter.repository.UserFilterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class UserFilterFixture {

    @Autowired
    private UserFilterRepository userFilterRepository;

    public UserFilter createSingleFilter(UUID userId, String name) {
        UserFilter filter = UserFilter.builder()
                .user(User.builder().id(userId).build())
                .filterName(name + " filter")
                .urlPattern("https://www." + name + ".com/tag/some-title")
                .domain("www." + name + ".com")
                .extractionRegex("/tag/([^/?#]+)")
                .captureGroupIndex(1)
                .priority(1)
                .build();
        return userFilterRepository.save(filter);
    }

    public List<UserFilter> createActiveFilters(UUID userId, int count) {
        return createFilters(userId, count, "asite", true);
    }

    public List<UserFilter> createInactiveFilters(UUID userId, int count) {
        return createFilters(userId, count, "isite", false);
    }

    public UserFilter createTestUserFilter(UUID userId) {
        UserFilter filter = UserFilter.builder()
                .user(User.builder().id(userId).build())
                .filterName("example")
                .urlPattern("https://example.com/tag/some-title")
                .domain("example.com")
                .extractionRegex("/tag/([^/?#]+)")
                .captureGroupIndex(1)
                .priority(1)
                .build();
        return userFilterRepository.save(filter);
    }

    public UserFilter createFilterWithPriority(UUID userId, String name, int priority) {
        UserFilter filter = UserFilter.builder()
                .user(User.builder().id(userId).build())
                .filterName(name)
                .urlPattern("https://example.com/" + name)
                .domain("example.com")
                .extractionRegex("/tag/([^/?#]+)")
                .captureGroupIndex(1)
                .priority(priority)
                .build();
        return userFilterRepository.save(filter);
    }

    private List<UserFilter> createFilters(UUID userId, int count,
                                           String prefix, boolean active) {
        List<UserFilter> filters = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String siteName = prefix + i;
            filters.add(UserFilter.builder()
                    .user(User.builder().id(userId).build())
                    .filterName(siteName + "filter")
                    .description("filter for " + siteName + ".com")
                    .urlPattern("https://www." + siteName + ".com/tag/some-title")
                    .domain("www." + siteName + ".com")
                    .extractionRegex("/tag/([^/?#]+)")
                    .captureGroupIndex(1)
                    .priority(1)
                    .active(active)
                    .build());
        }

        return userFilterRepository.saveAll(filters);
    }
}
