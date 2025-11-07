package com.sloyardms.stashbox.itemimage.dto;

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
public class ItemImageResponse {

    private UUID id;
    private String originalFilename;
    private String path;
    private String mimeType;
    private String fileExtension;

}
