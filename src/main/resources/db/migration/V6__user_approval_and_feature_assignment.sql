-- Add approved and full_name columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS approved BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(100);

-- Set existing users as approved (especially admin)
UPDATE users SET approved = true WHERE role = 'ADMIN';

-- Add assigned_to_id column to features table
ALTER TABLE features ADD COLUMN IF NOT EXISTS assigned_to_id UUID REFERENCES users(id) ON DELETE SET NULL;

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_features_assigned_to ON features(assigned_to_id);
CREATE INDEX IF NOT EXISTS idx_users_approved ON users(approved);
