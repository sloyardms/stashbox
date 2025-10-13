package com.sloyardms.backend.integration.group;

import com.sloyardms.backend.group.dto.ItemGroupCreateDto;

public class ItemGroupCreateDtoDataBuilder {

    private String name = "test group";
    private String description = "test description";

    private ItemGroupCreateDtoDataBuilder() {
    }

    public static ItemGroupCreateDtoDataBuilder aValidCreateDto() {
        return new ItemGroupCreateDtoDataBuilder();
    }

    public static ItemGroupCreateDtoDataBuilder anInvalidCreateDto() {
        return new ItemGroupCreateDtoDataBuilder()
                .withoutName()
                .withoutDescription();
    }

    ItemGroupCreateDtoDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    ItemGroupCreateDtoDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    ItemGroupCreateDtoDataBuilder withoutName() {
        this.name = null;
        return this;
    }

    ItemGroupCreateDtoDataBuilder withoutDescription() {
        this.description = null;
        return this;
    }

    ItemGroupCreateDto build() {
        return ItemGroupCreateDto.builder()
                .name(name)
                .description(description)
                .build();
    }

}

