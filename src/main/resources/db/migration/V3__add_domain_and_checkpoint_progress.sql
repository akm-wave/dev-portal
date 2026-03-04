-- Add domain column to features table
ALTER TABLE features ADD COLUMN IF NOT EXISTS domain VARCHAR(50) NOT NULL DEFAULT 'General';

-- Create feature_checkpoint_progress table for tracking checkpoint status per feature
CREATE TABLE IF NOT EXISTS feature_checkpoint_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    checklist_id UUID NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remark TEXT,
    attachment_url VARCHAR(500),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(feature_id, checklist_id)
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_fcp_feature_id ON feature_checkpoint_progress(feature_id);
CREATE INDEX IF NOT EXISTS idx_fcp_checklist_id ON feature_checkpoint_progress(checklist_id);
CREATE INDEX IF NOT EXISTS idx_features_domain ON features(domain);

-- Update existing features with appropriate domains based on their names
UPDATE features SET domain = 'KYC' WHERE LOWER(name) LIKE '%kyc%';
UPDATE features SET domain = 'Payments' WHERE LOWER(name) LIKE '%payment%' OR LOWER(name) LIKE '%wallet%';
UPDATE features SET domain = 'Fraud' WHERE LOWER(name) LIKE '%fraud%';
UPDATE features SET domain = 'Admin' WHERE LOWER(name) LIKE '%admin%';
UPDATE features SET domain = 'User Experience' WHERE LOWER(name) LIKE '%onboarding%' OR LOWER(name) LIKE '%user%';
