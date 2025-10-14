package com.sloyardms.backend.group.dto;

import com.sloyardms.backend.common.entity.UpdatableDto;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemGroupUpdateDto extends UpdatableDto {

    @Size(max = 50, message = "{itemGroup.name.maxSize}")
    private String name;

    @Size(max = 255, message = "{itemGroup.description.maxSize}")
    private String description;

    private Boolean defaultGroup;

    @Override
    public boolean hasAtLeastOneField() {
        return name != null ||
                description != null ||
                defaultGroup != null;
    }
}
