package com.sloyardms.stashbox.itemtag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CreateItemTagRequest {

    @NotBlank(message = "{itemTag.name.notBlank}")
    @Size(max = 50, message = "{itemTag.name.maxSize}")
    private String name;

}
