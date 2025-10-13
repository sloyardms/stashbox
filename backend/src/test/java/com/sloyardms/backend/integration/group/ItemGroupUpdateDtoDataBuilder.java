package com.sloyardms.backend.integration.group;

import com.sloyardms.backend.group.dto.ItemGroupUpdateDto;

public class ItemGroupUpdateDtoDataBuilder {

    private String name = "updated group";
    private String description = "updated description";
    private Boolean defaultGroup = true;

    private ItemGroupUpdateDtoDataBuilder() {
    }

    public static ItemGroupUpdateDtoDataBuilder aValidUpdateDto() {
        return new ItemGroupUpdateDtoDataBuilder();
    }

    public static ItemGroupUpdateDtoDataBuilder anEmptyUpdateDto() {
        return new ItemGroupUpdateDtoDataBuilder()
                .withoutName()
                .withoutDescription();
    }

    ItemGroupUpdateDtoDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    ItemGroupUpdateDtoDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    ItemGroupUpdateDtoDataBuilder withoutName() {
        this.name = null;
        return this;
    }

    ItemGroupUpdateDtoDataBuilder withoutDescription() {
        this.description = null;
        return this;
    }

    ItemGroupUpdateDtoDataBuilder asDefaultGroup() {
        this.defaultGroup = true;
        return this;
    }

    ItemGroupUpdateDtoDataBuilder asNonDefaultGroup() {
        this.defaultGroup = false;
        return this;
    }

    ItemGroupUpdateDto build() {
        return ItemGroupUpdateDto.builder()
                .name(name)
                .description(description)
                .defaultGroup(defaultGroup)
                .build();
    }

}
