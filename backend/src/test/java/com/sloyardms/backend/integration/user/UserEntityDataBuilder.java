package com.sloyardms.backend.integration.user;

import com.sloyardms.backend.user.UserRepository;
import com.sloyardms.backend.user.entity.User;
import com.sloyardms.backend.user.entity.UserSettings;

import java.util.UUID;

public class UserEntityDataBuilder {

    public static User aValidUser(){
        return User.builder()
                .id(UUID.randomUUID())
                .externalId(UUID.randomUUID())
                .userName("test_user")
                .settings(new UserSettings())
                .build();
    }

    public static User buildAndSave(UserRepository userRepository){
        return userRepository.save(aValidUser());
    }

}
