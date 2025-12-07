package com.sloyardms.stashbox.userfilter.specification;

import com.sloyardms.stashbox.common.specification.SpecificationUtils;
import com.sloyardms.stashbox.userfilter.entity.UserFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UserFilterSpecification extends SpecificationUtils {

    public static Specification<UserFilter> belongsToUser(UUID userExternalId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("externalId"), userExternalId);
    }

    public static Specification<UserFilter> active(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("active"), active);
        };
    }

    public static Specification<UserFilter> byDomain(String domain) {
        return (root, query, cb) -> {
            if (domain == null || domain.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("domain"), domain.trim());
        };
    }

    public static Specification<UserFilter> search(String searchQuery) {
        return (root, query, cb) -> {
            if (searchQuery == null || searchQuery.isBlank()) {
                return cb.conjunction();
            }
            String pattern = escapeLikePattern(searchQuery.trim().toLowerCase());
            return cb.or(
                    cb.like(root.get("normalizedFilterName"), pattern, '\\'),
                    cb.like(root.get("normalizedUrlPattern"), pattern, '\\')
            );
        };
    }

}
