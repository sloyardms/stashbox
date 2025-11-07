package com.sloyardms.stashbox.itemnote.dto;

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
public class UpdateItemNoteRequest {

    @Size(max = 500, message = "{itemNote.note.maxSize}")
    private String note;

}
