package com.sloyardms.backend.note.entity;

import com.sloyardms.backend.common.entity.Auditable;
import com.sloyardms.backend.note_file.entity.NoteFile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "item_notes",
        indexes = {
                @Index(name = "item_notes_item_id_index", columnList = "item_id")
        }
)
public class ItemNote extends Auditable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "note")
    private String note;

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "note_id")
    private List<NoteFile> files = new ArrayList<>();

}
