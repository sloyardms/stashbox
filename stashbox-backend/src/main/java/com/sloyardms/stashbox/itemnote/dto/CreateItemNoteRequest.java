package com.sloyardms.stashbox.itemnote.dto;

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
public class CreateItemNoteRequest {

    @NotBlank(message = "{itemNote.note.notBlank}")
    @Size(max = 500, message = "{itemNote.note.maxSize}")
    private String note;

}
