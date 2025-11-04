-- Add a generated tsvector column
ALTER TABLE stash_items
    ADD COLUMN search_vector tsvector
        GENERATED ALWAYS AS (
            setweight(to_tsvector('english', coalesce(normalized_title, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(url, '')), 'B') ||
            setweight(to_tsvector('english', coalesce(description, '')), 'C')
            ) STORED;

CREATE INDEX stash_items_search_vector_idx ON stash_items
    USING GIN(search_vector) WHERE deleted_at IS NULL;