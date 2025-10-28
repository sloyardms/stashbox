-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY,
    external_id UUID NOT NULL UNIQUE,
    username TEXT NOT NULL UNIQUE,
    settings JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- User Filters
CREATE TABLE user_filters (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    filter_name TEXT NOT NULL,
    url_pattern TEXT NOT NULL,
    extraction_regex TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX user_filters_user_id_index ON user_filters(user_id);
CREATE UNIQUE INDEX user_filter_url_pattern_unique_idx ON user_filters (user_id, LOWER(url_pattern));

-- Item Groups
CREATE TABLE item_groups (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    normalized_name TEXT NOT NULL,
    slug TEXT NOT NULL,
    description TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX item_groups_user_id_index ON item_groups(user_id);
CREATE UNIQUE INDEX item_groups_name_unique_idx ON item_groups (user_id, normalized_name);
CREATE UNIQUE INDEX item_groups_slug_unique_idx ON item_groups (user_id, slug);


-- Item Images
CREATE TABLE item_images (
    id UUID PRIMARY KEY,
    original_filename TEXT NOT NULL,
    stored_filename TEXT NOT NULL,
    file_path TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    file_extension TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Items (stash_items)
CREATE TABLE stash_items (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id UUID NOT NULL REFERENCES item_groups(id),
    title TEXT,
    normalized_title TEXT,
    slug TEXT,
    url TEXT,
    description TEXT,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    image_id UUID UNIQUE REFERENCES item_images(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX stash_items_user_id_index ON stash_items(user_id);
CREATE INDEX stash_items_group_id_index ON stash_items(group_id);
CREATE INDEX stash_items_favorites_index ON stash_items(user_id, is_favorite) WHERE is_favorite = TRUE;
CREATE INDEX stash_items_active_items_idx ON stash_items(user_id, group_id) WHERE deleted_at IS NULL;
CREATE INDEX stash_items_group_id_created_at_index ON stash_items(group_id, created_at DESC);
CREATE UNIQUE INDEX stash_items_user_id_url_unique ON stash_items (user_id, url);
CREATE UNIQUE INDEX stash_items_title_unique_idx ON stash_items (user_id, normalized_title);
CREATE UNIQUE INDEX stash_items_slug_unique_idx ON stash_items (user_id, slug);

-- Tags
CREATE TABLE tags (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    normalized_name TEXT NOT NULL,
    slug TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX tags_user_id_index ON tags(user_id);
CREATE UNIQUE INDEX tags_name_unique_idx ON tags (user_id, normalized_name);
CREATE UNIQUE INDEX tags_slug_unique_idx ON tags (user_id, slug);

-- Item Tags (many-to-many)
CREATE TABLE item_tags (
    item_id UUID NOT NULL REFERENCES stash_items(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (item_id, tag_id)
);
CREATE INDEX item_tags_tag_id_idx ON item_tags (tag_id);

-- Item Notes
CREATE TABLE item_notes (
    id UUID PRIMARY KEY,
    item_id UUID NOT NULL REFERENCES stash_items(id) ON DELETE CASCADE,
    note TEXT,
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