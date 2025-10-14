package com.sloyardms.backend.tag.dto;

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
public class TagUpdateDto extends UpdatableDto {

    @Size(max = 50, message = "{tag.name.maxSize}")
    private String name;

    @Override
    public boolean hasAtLeastOneField() {
        return name != null;
    }

}
