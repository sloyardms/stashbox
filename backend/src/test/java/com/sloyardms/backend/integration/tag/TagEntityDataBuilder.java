package com.sloyardms.backend.integration.tag;

import com.sloyardms.backend.tag.TagRepository;
import com.sloyardms.backend.tag.entity.Tag;
import com.sloyardms.backend.user.entity.User;

import java.util.UUID;

public class TagEntityDataBuilder {

    private UUID id = UUID.randomUUID();
    private UUID userId;
    private String name = "test name";

    private TagEntityDataBuilder() {
    }

    public static TagEntityDataBuilder aValidTag(UUID userId) {
        TagEntityDataBuilder builder = new TagEntityDataBuilder();
        builder.userId = userId;
        return builder;
    }

    public TagEntityDataBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public TagEntityDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TagEntityDataBuilder withUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public Tag build() {
        return Tag.builder()
                .id(id)
                .user(User.builder().id(userId).build())
                .name(name)
                .build();
    }

    public Tag buildAndSave(TagRepository repository) {
        return repository.save(build());
    }

}

