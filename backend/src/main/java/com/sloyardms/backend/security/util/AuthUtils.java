package com.sloyardms.backend.security.util;

import com.sloyardms.backend.security.model.DevUserPrincipal;
import com.sloyardms.backend.security.model.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class to extract current user information from the security context
 * Works with bot JWT tokens (production) and fake authentication (dev/text)
 */
public class AuthUtils {

    /**
     * Get the current authenticated user's UUID
     *
     * @return the user id (UUID) from the "sub" claim
     * @throws SecurityException if no authenticated user found
     * @throws IllegalStateException if principal type is not supported
     */
    public static UUID getCurrentUserExternalId() {
        Authentication auth = getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof DevUserPrincipal fakeUser) {
            return fakeUser.getUserId();
        }

        if (principal instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new IllegalStateException("JWT subject is null or empty");
            }
            return parseUUID(subject, "JWT subject");
        }

        if (principal instanceof OidcUser oidcUser) {
            String subject = oidcUser.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new IllegalStateException("OIDC subject is null or empty");
            }
            return parseUUID(subject, "OIDC subject");
        }

        throw new IllegalStateException("Unknown principal type: " + principal.getClass());
    }

    /**
     * Get the current authenticated user's username
     *
     * @return the username from the 'preferred_username' claim
     * @throws SecurityException if no authenticated user found
     * @throws IllegalStateException if principal type is not supported
     */
    public static String getCurrentUsername() {
        Authentication auth = getAuthentication();
        Object principal = auth.getPrincipal();

        // For dev/test environment
        if (principal instanceof DevUserPrincipal fake) {
            return fake.getUsername();
        }

        // For JWT tokens
        if (principal instanceof Jwt jwt) {
            String username = jwt.getClaimAsString("preferred_username");
            if (username == null || username.isBlank()) {
                username = jwt.getClaimAsString("username");
            }
            if (username == null || username.isBlank()) {
                username = jwt.getClaimAsString("email");
            }
            if (username == null || username.isBlank()) {
                throw new IllegalStateException("No valid username claim found in JWT");
            }
            return username;
        }

        // For OIDC user
        if (principal instanceof OidcUser oidcUser) {
            String username = oidcUser.getPreferredUsername();
            if (username == null || username.isBlank()) {
                username = oidcUser.getEmail();
            }
            if (username == null || username.isBlank()) {
                throw new IllegalStateException("No valid username found in OIDC user");
            }
            return username;
        }
        throw new IllegalStateException("Unknown principal type: " + principal.getClass());
    }

    /**
     * Check if the current user has a specific role
     *
     * @param role the role to check
     * @return true if user has the role
     */
    public static boolean hasRole(UserRole role) {
        Authentication auth = getAuthentication();
        String authority = role.getValue();

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    /**
     * Check if the current user has any of the specified roles
     * @param roles the roles to check
     * @return true if user has at least one of the roles
     */
    public static boolean hasAnyRole(UserRole... roles) {
        for (UserRole role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the current user has all of the specified roles
     * @param roles the roles to check
     * @return true if user has all of the roles
     */
    public static boolean hasAllRoles(UserRole... roles) {
        for (UserRole role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the current user is an admin
     * @return true if user has the admin role
     */
    public static boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    /**
     * Get all roles for the current user
     * @return immutable set of roles
     */
    public static Set<UserRole> getCurrentUserRoles() {
        Authentication auth = getAuthentication();

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(UserRole::fromValue)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Get the authentication object from security context
     * @return Authentication object
     * @throws SecurityException if no authenticated user found
     */
    private static Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("No authenticated user found in security context");
        }

        // Check for anonymous authentication
        if ("anonymousUser".equals(auth.getPrincipal())) {
            throw new SecurityException("Anonymous user is not considered authenticated");
        }

        return auth;
    }

    private static UUID parseUUID(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    fieldName + " is not a valid UUID: " + value, e
            );
        }
    }

}

