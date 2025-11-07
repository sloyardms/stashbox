package com.sloyardms.stashbox.stashitem.dto;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CreateStashItemRequest {

    @Size(max = 255, message = "{stashItem.title.maxSize}")
    private String title;

    @Size(max = 1000, message = "{stashItem.url.maxSize}")
    private String url;

    @Size(max = 500, message = "{stashItem.description.maxSize}")
    private String description;

    private List<String> tags;

}