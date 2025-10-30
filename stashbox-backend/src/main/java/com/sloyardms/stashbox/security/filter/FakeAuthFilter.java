package com.sloyardms.stashbox.security.filter;

import com.sloyardms.stashbox.security.model.DevUserPrincipal;
import com.sloyardms.stashbox.security.model.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Filter to fake authentication for dev/test purposes
 * Mimics the structure of a real JWT token from keycloak
 */
public class FakeAuthFilter extends OncePerRequestFilter {

    public static final UUID DEFAULT_EXTERNAL_USER_ID = UUID.fromString("c8aa58be-03a9-458e-94f7-d708b814d2cc");
    public static final UUID DEFAULT_USER_ID = UUID.fromString("b5e0f36e-7b68-44d9-9b8b-dc26f3a0f3cb");
    public static final String DEFAULT_USERNAME = "dev_user";
    public static final String DEFAULT_FULL_NAME = "Dev User";

    private final UUID userId;
    private final String username;
    private final String fullName;
    private final List<SimpleGrantedAuthority> authorities;

    public static final List<SimpleGrantedAuthority> AUTHORITIES = List.of(
            new SimpleGrantedAuthority("admin"),
            new SimpleGrantedAuthority("user"));

    /**
     * Creates a filter with default dev user (admin + user roles)
     */
    public FakeAuthFilter() {
        this(DEFAULT_EXTERNAL_USER_ID, DEFAULT_USERNAME, DEFAULT_FULL_NAME,
                List.of(UserRole.ADMIN, UserRole.USER));
    }

    /**
     * Creates a filter with custom user and roles
     *
     * @param userId   the fake user's UUID
     * @param username the fake user's username
     * @param fullName the fake user's full name
     * @param roles    the roles to assign to the fake user
     */
    public FakeAuthFilter(UUID userId, String username, String fullName, List<UserRole> roles) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getValue()))
                .toList();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Create fake user principal matching JWT token structure
        DevUserPrincipal principal = new DevUserPrincipal(
                userId,
                username,
                fullName
        );

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                AUTHORITIES
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear security context after request
            SecurityContextHolder.clearContext();
        }
    }
}
