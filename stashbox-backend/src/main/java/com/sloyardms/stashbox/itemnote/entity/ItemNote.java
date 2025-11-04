package com.sloyardms.stashbox.itemnote.entity;

import com.sloyardms.stashbox.common.entity.Auditable;
import com.sloyardms.stashbox.notefile.entity.NoteFile;
import com.sloyardms.stashbox.stashitem.entity.StashItem;
import com.sloyardms.stashbox.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "item_notes", indexes = {
        @Index(name = "item_notes_item_user_created_index", columnList = "item_id, user_id, created_at")
})
public class ItemNote extends Auditable {

    @Id
    @Column(name = "id")
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private StashItem item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ToString.Include
    @Column(name = "note", length = 500)
    private String note;

    @Builder.Default
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private List<NoteFile> noteFiles = new ArrayList<>();

}
