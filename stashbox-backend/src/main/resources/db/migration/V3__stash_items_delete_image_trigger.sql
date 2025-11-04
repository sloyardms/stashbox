-- Trigger for deleting images of deleted stash_items
CREATE OR REPLACE FUNCTION delete_orphaned_images()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.image_id IS NOT NULL THEN
DELETE FROM item_images WHERE id = OLD.image_id;
END IF;
RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER stash_items_delete_image_trigger
AFTER DELETE ON stash_items
FOR EACH ROW
EXECUTE FUNCTION delete_orphaned_images();