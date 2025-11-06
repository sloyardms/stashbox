package com.sloyardms.stashbox.itemtag.dto;

import com.sloyardms.stashbox.common.annotations.AtLeastOneNonNullField;
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
@AtLeastOneNonNullField
public class UpdateItemTagRequest {

    @Size(max = 50, message = "{itemTag.name.maxSize}")
    private String name;

}
