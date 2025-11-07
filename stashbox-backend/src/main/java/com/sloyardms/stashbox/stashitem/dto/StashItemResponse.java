package com.sloyardms.stashbox.stashitem.dto;

import com.sloyardms.stashbox.itemgroup.dto.ItemGroupResponse;
import com.sloyardms.stashbox.itemimage.dto.ItemImageResponse;
import com.sloyardms.stashbox.itemtag.dto.ItemTagResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StashItemResponse {

    private UUID id;
    private ItemGroupResponse group;
    private String title;
    private String slug;
    private String url;
    private String description;
    private boolean favorite;
    private ItemImageResponse image;
    private OffsetDateTime deletedAt;
    private List<ItemTagResponse> tags;

}
