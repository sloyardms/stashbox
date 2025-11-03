-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY,
    external_id UUID NOT NULL,
    username TEXT NOT NULL,
    settings JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT users_external_id_unique UNIQUE (external_id),
    CONSTRAINT users_username_unique UNIQUE (username)
);

-- User Filters
CREATE TABLE user_filters (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    -- Filter identification
    filter_name TEXT NOT NULL,
    normalized_filter_name TEXT NOT NULL,
    description TEXT,
    -- Pattern matching
    url_pattern TEXT NOT NULL,
    normalized_url_pattern TEXT NOT NULL,
    domain_filter TEXT,
    extraction_regex TEXT NOT NULL,
    capture_group_index INTEGER NOT NULL DEFAULT 1,
    -- Ordering and state
    priority INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    -- Usage statistics
    match_count BIGINT NOT NULL DEFAULT 0,
    last_matched_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT user_filters_normalized_filter_name_unique UNIQUE (user_id, normalized_filter_name),
    CONSTRAINT user_filters_normalized_url_pattern_unique UNIQUE (user_id, normalized_url_pattern),
    CONSTRAINT user_filters_priority_positive CHECK (priority >= 0),
    CONSTRAINT user_filters_capture_group_positive CHECK (capture_group_index > 0)
);
CREATE INDEX user_filters_user_id_index ON user_filters(user_id);
CREATE INDEX user_filters_user_id_active_index ON user_filters(user_id, is_active);

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
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT item_groups_name_unique UNIQUE (user_id, normalized_name),
    CONSTRAINT item_groups_slug_unique UNIQUE (user_id, slug)
);
CREATE INDEX item_groups_user_id_index ON item_groups(user_id);


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