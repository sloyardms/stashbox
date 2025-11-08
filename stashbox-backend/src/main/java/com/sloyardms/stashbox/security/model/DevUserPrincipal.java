package com.sloyardms.stashbox.security.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class DevUserPrincipal {

    private UUID userId;
    private String username;
    private String fullName;
    private String email;

    public DevUserPrincipal(UUID userId, String username, String fullName, String email) {
        if (userId == null || username == null || fullName == null) {
            throw new IllegalArgumentException("userId, username, fullName and email cannot be null");
        }
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
    }

}
