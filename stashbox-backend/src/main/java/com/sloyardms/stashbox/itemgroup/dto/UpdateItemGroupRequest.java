package com.sloyardms.stashbox.itemgroup.dto;

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
public class UpdateItemGroupRequest {

    @Size(max = 50, message = "{itemGroup.name.maxSize}")
    private String name;

    @Size(max = 255, message = "{itemGroup.description.maxSize}")
    private String description;

}
