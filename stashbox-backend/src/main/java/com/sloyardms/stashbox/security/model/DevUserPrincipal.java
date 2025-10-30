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

    public DevUserPrincipal(UUID userId, String username, String fullName) {
        if (userId == null || username == null || fullName == null) {
            throw new IllegalArgumentException("userId, username, and fullName cannot be null");
        }
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
    }

}
