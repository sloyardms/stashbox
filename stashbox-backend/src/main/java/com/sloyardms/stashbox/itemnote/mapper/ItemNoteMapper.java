package com.sloyardms.stashbox.itemnote.mapper;

import com.sloyardms.stashbox.itemnote.dto.CreateItemNoteRequest;
import com.sloyardms.stashbox.itemnote.dto.ItemNoteResponse;
import com.sloyardms.stashbox.itemnote.dto.UpdateItemNoteRequest;
import com.sloyardms.stashbox.itemnote.entity.ItemNote;
import com.sloyardms.stashbox.notefile.mapper.NoteFileMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {NoteFileMapper.class})
public interface ItemNoteMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "noteFiles", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "id", ignore = true)
    ItemNote toEntity(CreateItemNoteRequest createItemNoteRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "noteFiles", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "id", ignore = true)
    ItemNote updateFromRequest(UpdateItemNoteRequest updateItemNoteRequest, @MappingTarget ItemNote itemNote);

    ItemNoteResponse toResponse(ItemNote itemNote);

}
