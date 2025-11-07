package com.sloyardms.stashbox.stashitem.dto;

import com.sloyardms.stashbox.itemimage.dto.ItemImageResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StashItemSummaryResponse {

    private UUID id;
    private String title;
    private String slug;
    private boolean favorite;
    private ItemImageResponse image;

}
