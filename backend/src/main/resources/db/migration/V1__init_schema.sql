-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY,
    external_id UUID NOT NULL UNIQUE,
    settings JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Item Groups
CREATE TABLE item_groups (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT item_groups_user_id_name_unique UNIQUE(user_id, name)
);
CREATE INDEX item_groups_user_id_index ON item_groups(user_id);

-- Items (stashitems)
CREATE TABLE items (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id UUID NOT NULL REFERENCES item_groups(id) ON DELETE SET DEFAULT,
    title TEXT,
    url TEXT,
    description TEXT,
    favorited BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT items_user_id_title_unique UNIQUE(user_id, title),
    CONSTRAINT items_user_id_url_unique UNIQUE(user_id, url)
);
CREATE INDEX items_user_id_index ON items(user_id);
CREATE INDEX items_group_id_index ON items(group_id);

-- Tags
CREATE TABLE tags (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT tags_user_id_name_unique UNIQUE(user_id, name)
);
CREATE INDEX tags_user_id_index ON tags(user_id);

-- Item Tags (many-to-many)
CREATE TABLE item_tags (
    item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    CONSTRAINT item_tags_item_id_tag_id_unique UNIQUE(item_id, tag_id)
);

-- Item Notes
CREATE TABLE item_notes (
    id UUID PRIMARY KEY,
    item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    note TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX item_notes_item_id_index ON item_notes(item_id);

-- Note Files
CREATE TABLE note_files (
    id UUID PRIMARY KEY,
    note_id UUID NOT NULL REFERENCES item_notes(id) ON DELETE CASCADE,
    original_filename TEXT NOT NULL,
    stored_filename TEXT NOT NULL,
    file_path TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    file_extension TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX note_files_note_id_index ON note_files(note_id);

-- Item Images
CREATE TABLE item_images (
    id UUID PRIMARY KEY,
    item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    original_filename TEXT NOT NULL,
    stored_filename TEXT NOT NULL,
    file_path TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    file_extension TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX item_images_item_id_index ON item_images(item_id);

-- Tag Usage Summary (analytics)
CREATE TABLE tag_usage_summary (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    group_id UUID NOT NULL REFERENCES item_groups(id) ON DELETE CASCADE,
    item_count INTEGER NOT NULL,
    PRIMARY KEY(user_id, group_id, tag_id)
);
