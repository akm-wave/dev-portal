-- Add mongo_file_id and attachment_filename to feature_checkpoints for attachment support
ALTER TABLE feature_checkpoints ADD COLUMN IF NOT EXISTS mongo_file_id VARCHAR(100);
ALTER TABLE feature_checkpoints ADD COLUMN IF NOT EXISTS attachment_filename VARCHAR(255);
