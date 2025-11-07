package com.sloyardms.stashbox.stashitem.dto;

import com.sloyardms.stashbox.common.annotations.AtLeastOneNonNullField;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AtLeastOneNonNullField
public class UpdateStashItemRequest {

    private UUID groupId;

    @Size(max = 255, message = "{stashItem.title.maxSize}")
    private String title;

    @Size(max = 1000, message = "{stashItem.url.maxSize}")
    private String url;

    @Size(max = 500, message = "{stashItem.description.maxSize}")
    private String description;

    private Boolean favorite;

    private List<String> tags;

}
