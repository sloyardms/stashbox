package com.sloyardms.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Utility class to get the current user id from the security context
 */
public class AuthUtils {

    /**
     * Get the current user id from the security context
     * @return the user id (UUID)
     */
    public static UUID getCurrentUserId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null || !auth.isAuthenticated()){
            throw new SecurityException("No authenticated user found in security context");
        }

        Object principal = auth.getPrincipal();

        if(principal instanceof String s) return UUID.fromString(s);
        if(principal instanceof Jwt jwt) return UUID.fromString(jwt.getSubject());
        if(principal instanceof OidcUser oidcUser) return UUID.fromString(oidcUser.getSubject());

        throw new IllegalStateException("Unknown principal type: " + principal.getClass());
    }

}
