package com.sloyardms.stashbox.security.utils;

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
     * @return the username from the 'preferred_username' or 'username' claim
     * @throws SecurityException     if no authenticated user found
     * @throws IllegalStateException if principal type is not supported
     */
    public static String getCurrentUsername() {
        Authentication auth = getAuthentication();
        Object principal = auth.getPrincipal();

        // For JWT tokens
        if (principal instanceof Jwt jwt) {
            String username = jwt.getClaimAsString("preferred_username");
            if (username == null || username.isBlank()) {
                username = jwt.getClaimAsString("username");
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
     * Get the current authenticated user's email
     *
     * @return the email from the 'email' claim
     * @throws SecurityException     if no authenticated user found
     * @throws IllegalStateException if principal type is not supported
     */
    public static String getCurrentUserEmail() {
        Authentication auth = getAuthentication();
        Object principal = auth.getPrincipal();

        // For JWT tokens
        if (principal instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            if (email == null || email.isBlank()) {
                throw new IllegalStateException("No valid email claim found in JWT");
            }
            return email;
        }

        // For OIDC user
        if (principal instanceof OidcUser oidcUser) {
            String email = oidcUser.getPreferredUsername();
            if (email == null || email.isBlank()) {
                email = oidcUser.getEmail();
            }
            if (email == null || email.isBlank()) {
                throw new IllegalStateException("No valid email found in OIDC user");
            }
            return email;
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
