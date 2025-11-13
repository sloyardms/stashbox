package com.sloyardms.stashbox.user.specification;

import com.sloyardms.stashbox.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UserSpecification {

    public static Specification<User> excludingUser(UUID userExternalId) {
        return (root, query, cb) ->
                cb.notEqual(root.get("externalId"), userExternalId);
    }

    public static Specification<User> search(String searchQuery) {
        return (root, query, cb) -> {
            if (searchQuery == null || searchQuery.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + searchQuery.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("username")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }
}
