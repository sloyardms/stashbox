package com.sloyardms.stashbox.security.utils;

import com.sloyardms.stashbox.security.model.DevUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Utility class to extract current user information from the security context
 * Works with bot JWT tokens (production) and fake authentication (dev/text)
 */
public class AuthUtils {

    /**
     * Get the current authenticated user's UUID
     *
     * @return the user id (UUID) from the "sub" claim
     * @throws SecurityException     if no authenticated user found
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
     * @throws SecurityException     if no authenticated user found
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
     * Get the authentication object from security context
     *
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
