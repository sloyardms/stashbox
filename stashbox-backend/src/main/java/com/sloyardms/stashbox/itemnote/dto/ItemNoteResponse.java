package com.sloyardms.stashbox.itemnote.dto;

import com.sloyardms.stashbox.notefile.dto.NoteFileResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ItemNoteResponse {

    private UUID id;
    private String note;
    private List<NoteFileResponse> noteFiles;
    private Instant createdAt;
    private Instant updatedAt;

}
