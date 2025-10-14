package com.sloyardms.backend.tag.dto;

import jakarta.validation.constraints.NotBlank;
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
public class TagCreateDto {

    @NotBlank(message = "{tag.name.notBlank}")
    @Size(max = 50, message = "{tag.name.maxSize}")
    private String name;

}
