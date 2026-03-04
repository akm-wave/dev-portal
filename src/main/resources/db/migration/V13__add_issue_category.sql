-- Add category column to issues table
ALTER TABLE issues ADD COLUMN IF NOT EXISTS category VARCHAR(30) DEFAULT 'OTHER';

-- Update existing issues to have a default category
UPDATE issues SET category = 'OTHER' WHERE category IS NULL;
