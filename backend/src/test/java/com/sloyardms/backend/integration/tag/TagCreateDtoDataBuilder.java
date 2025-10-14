package com.sloyardms.backend.integration.tag;

import com.sloyardms.backend.tag.dto.TagCreateDto;

public class TagCreateDtoDataBuilder {

    private String name = "test tag";

    private TagCreateDtoDataBuilder() {
    }

    public static TagCreateDtoDataBuilder aValidCreateDto() {
        return new TagCreateDtoDataBuilder();
    }

    public static TagCreateDtoDataBuilder anInvalidCreateDto() {
        return new TagCreateDtoDataBuilder()
                .withoutName();
    }

    TagCreateDtoDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    TagCreateDtoDataBuilder withoutName() {
        this.name = null;
        return this;
    }

    TagCreateDto build() {
        return TagCreateDto.builder()
                .name(name)
                .build();
    }

}

