-- Add missing fields for productivity tracking
-- These fields are needed for the Developer Productivity Dashboard

-- Add due_date to feature_checkpoints for tracking overdue checkpoints
ALTER TABLE feature_checkpoints ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;

-- Add weight to checklists for productivity scoring (already exists in V21 but ensuring it's there)
-- Note: V21__checklist_weight_assignment.sql already added this, but we ensure it exists

-- Add assigned_to to feature_checkpoints for user-specific tracking
ALTER TABLE feature_checkpoints ADD COLUMN IF NOT EXISTS assigned_to_id UUID REFERENCES users(id) ON DELETE SET NULL;

-- Add completed_at to features for completion tracking
ALTER TABLE features ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;

-- Add due_date to incidents for tracking overdue incidents
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;

-- Add due_date to hotfixes for tracking overdue hotfixes  
ALTER TABLE hotfixes ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;

-- Add due_date to issues for tracking overdue issues
ALTER TABLE issues ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;

-- Add weight to issues for productivity scoring
ALTER TABLE issues ADD COLUMN IF NOT EXISTS weight INTEGER DEFAULT 3;

-- Add weight to incidents for productivity scoring
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS weight INTEGER DEFAULT 5;

-- Add weight to hotfixes for productivity scoring
ALTER TABLE hotfixes ADD COLUMN IF NOT EXISTS weight INTEGER DEFAULT 5;

-- Add weight to features for productivity scoring
ALTER TABLE features ADD COLUMN IF NOT EXISTS weight INTEGER DEFAULT 10;

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_feature_checkpoints_assigned_to ON feature_checkpoints(assigned_to_id);
CREATE INDEX IF NOT EXISTS idx_feature_checkpoints_due_date ON feature_checkpoints(due_date);
CREATE INDEX IF NOT EXISTS idx_feature_checkpoints_status_assigned ON feature_checkpoints(status, assigned_to_id);

CREATE INDEX IF NOT EXISTS idx_features_assigned_to_completed ON features(owner_id, completed_at);
CREATE INDEX IF NOT EXISTS idx_features_due_date ON features(target_date);
CREATE INDEX IF NOT EXISTS idx_features_status_assigned ON features(status, owner_id);

CREATE INDEX IF NOT EXISTS idx_incidents_owner_due_date ON incidents(owner_id, due_date);
CREATE INDEX IF NOT EXISTS idx_incidents_status_owner ON incidents(status, owner_id);

CREATE INDEX IF NOT EXISTS idx_hotfixes_owner_due_date ON hotfixes(owner_id, due_date);
CREATE INDEX IF NOT EXISTS idx_hotfixes_status_owner ON hotfixes(status, owner_id);

CREATE INDEX IF NOT EXISTS idx_issues_assigned_to_due_date ON issues(assigned_to, due_date);
CREATE INDEX IF NOT EXISTS idx_issues_status_assigned ON issues(status, assigned_to);

-- Set default weights based on entity type
UPDATE features SET weight = 10 WHERE weight IS NULL;
UPDATE incidents SET weight = 5 WHERE weight IS NULL;
UPDATE hotfixes SET weight = 5 WHERE weight IS NULL;
UPDATE issues SET weight = 3 WHERE weight IS NULL;

-- Add comments for documentation
COMMENT ON COLUMN feature_checkpoints.due_date IS 'Due date for checkpoint completion, used for overdue tracking';
COMMENT ON COLUMN feature_checkpoints.assigned_to_id IS 'User assigned to this checkpoint, used for personal productivity tracking';
COMMENT ON COLUMN features.completed_at IS 'Timestamp when feature was completed, used for productivity metrics';
COMMENT ON COLUMN incidents.due_date IS 'Due date for incident resolution, used for overdue tracking';
COMMENT ON COLUMN hotfixes.due_date IS 'Due date for hotfix deployment, used for overdue tracking';
COMMENT ON COLUMN issues.due_date IS 'Due date for issue resolution, used for overdue tracking';
COMMENT ON COLUMN features.weight IS 'Productivity weight for completed features (10 points)';
COMMENT ON COLUMN incidents.weight IS 'Productivity weight for resolved incidents (5 points)';
COMMENT ON COLUMN hotfixes.weight IS 'Productivity weight for deployed hotfixes (5 points)';
COMMENT ON COLUMN issues.weight IS 'Productivity weight for resolved issues (3 points)';
