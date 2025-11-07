package com.sloyardms.stashbox.notefile.dto;

import com.sloyardms.stashbox.notefile.entity.UploadStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class NoteFileResponse {

    private UUID id;
    private String originalFilename;
    private String path;
    private String mimeType;
    private String fileExtension;
    private Integer displayOrder;
    private UploadStatus uploadStatus;
    private Instant createdAt;

}
