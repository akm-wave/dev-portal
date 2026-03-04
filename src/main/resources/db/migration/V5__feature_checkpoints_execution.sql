-- Feature Checkpoints table for feature-specific checkpoint execution
-- Checklist becomes template-only, execution status is tracked per feature

CREATE TABLE IF NOT EXISTS feature_checkpoints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    checklist_id UUID NOT NULL REFERENCES checklists(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remark TEXT,
    attachment_url VARCHAR(500),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(feature_id, checklist_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_feature_checkpoints_feature ON feature_checkpoints(feature_id);
CREATE INDEX IF NOT EXISTS idx_feature_checkpoints_checklist ON feature_checkpoints(checklist_id);
CREATE INDEX IF NOT EXISTS idx_feature_checkpoints_status ON feature_checkpoints(status);

-- Note: Checklist status column is deprecated but kept for backward compatibility
-- All status tracking should now happen in feature_checkpoints table
