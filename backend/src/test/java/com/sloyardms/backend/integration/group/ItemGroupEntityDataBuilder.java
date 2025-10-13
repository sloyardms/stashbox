package com.sloyardms.backend.integration.group;

import com.sloyardms.backend.group.ItemGroupRepository;
import com.sloyardms.backend.group.entity.ItemGroup;
import com.sloyardms.backend.user.entity.User;

import java.util.UUID;

public class ItemGroupEntityDataBuilder {

    private UUID id = UUID.randomUUID();
    private UUID userId;
    private String name = "test group";
    private String description = "test description";
    private boolean defaultGroup = true;

    private ItemGroupEntityDataBuilder() {
    }

    public static ItemGroupEntityDataBuilder aValidGroup(UUID userId) {
        ItemGroupEntityDataBuilder builder = new ItemGroupEntityDataBuilder();
        builder.userId = userId;
        return builder;
    }

    public ItemGroupEntityDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public ItemGroupEntityDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ItemGroupEntityDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ItemGroupEntityDataBuilder withDefaultGroup(boolean defaultGroup) {
        this.defaultGroup = defaultGroup;
        return this;
    }

    public ItemGroup build() {
        return ItemGroup.builder()
                .id(id)
                .user(User.builder().id(userId).build())
                .name(name)
                .description(description)
                .defaultGroup(defaultGroup)
                .build();
    }

    public ItemGroup buildAndSave(ItemGroupRepository repository) {
        return repository.save(build());
    }

}
