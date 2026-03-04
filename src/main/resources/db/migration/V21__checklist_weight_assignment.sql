-- V21: Add weight and assigned_to columns to checklists for gamified progress tracking

-- Add weight column to checklists (default 1 for equal weighting)
ALTER TABLE checklists ADD COLUMN IF NOT EXISTS weight INTEGER DEFAULT 1;

-- Add assigned_to column to checklists (references users table)
ALTER TABLE checklists ADD COLUMN IF NOT EXISTS assigned_to UUID;

-- Add foreign key constraint for assigned_to
ALTER TABLE checklists 
ADD CONSTRAINT fk_checklist_assigned_to 
FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL;

-- Create index for faster lookups by assigned user
CREATE INDEX IF NOT EXISTS idx_checklists_assigned_to ON checklists(assigned_to);

-- Create index for status lookups (for progress calculation)
CREATE INDEX IF NOT EXISTS idx_checklists_status ON checklists(status);

-- Update existing checklists to have default weight of 1
UPDATE checklists SET weight = 1 WHERE weight IS NULL;
