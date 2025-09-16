--  function for creating a default group for a new user
CREATE OR REPLACE FUNCTION create_default_group()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO item_groups (id, user_id, name, description, is_default, created_at, updated_at)
    VALUES (gen_random_uuid(), NEW.id, 'Ungrouped', 'Default group', TRUE, now(), now());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- trigger to call create_default_group
CREATE TRIGGER after_user_insert
AFTER INSERT ON users 
FOR EACH ROW 
EXECUTE FUNCTION create_default_group();