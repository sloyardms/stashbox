package com.sloyardms.stashbox.notefile.mapper;

import com.sloyardms.stashbox.notefile.dto.NoteFileResponse;
import com.sloyardms.stashbox.notefile.entity.NoteFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NoteFileMapper {

    @Mapping(target = "path", source = "filePath")
    NoteFileResponse toRespponse(NoteFile noteFile);

}
