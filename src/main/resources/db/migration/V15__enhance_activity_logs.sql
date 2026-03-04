-- Add new columns to activity_logs for comprehensive audit logging
ALTER TABLE activity_logs ADD COLUMN IF NOT EXISTS ip_address VARCHAR(50);
ALTER TABLE activity_logs ADD COLUMN IF NOT EXISTS old_value TEXT;
ALTER TABLE activity_logs ADD COLUMN IF NOT EXISTS new_value TEXT;

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_activity_logs_entity_type ON activity_logs(entity_type);
CREATE INDEX IF NOT EXISTS idx_activity_logs_action ON activity_logs(action);
CREATE INDEX IF NOT EXISTS idx_activity_logs_user_id ON activity_logs(user_id);
