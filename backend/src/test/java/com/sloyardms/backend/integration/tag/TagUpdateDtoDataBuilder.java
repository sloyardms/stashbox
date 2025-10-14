package com.sloyardms.backend.integration.tag;

import com.sloyardms.backend.tag.dto.TagUpdateDto;

public class TagUpdateDtoDataBuilder {

    private String name = "updated tag";

    private TagUpdateDtoDataBuilder() {
    }

    public static TagUpdateDtoDataBuilder aValidUpdateDto() {
        return new TagUpdateDtoDataBuilder();
    }

    public static TagUpdateDtoDataBuilder anEmptyUpdateDto() {
        return new TagUpdateDtoDataBuilder()
                .withoutName();
    }

    TagUpdateDtoDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    TagUpdateDtoDataBuilder withoutName() {
        this.name = null;
        return this;
    }

    TagUpdateDto build() {
        return TagUpdateDto.builder()
                .name(name)
                .build();
    }

}

