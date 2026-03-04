-- Create domains table
CREATE TABLE IF NOT EXISTS domains (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    color_code VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create incident_checklist_progress table
CREATE TABLE IF NOT EXISTS incident_checklist_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    checklist_id UUID NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remark TEXT,
    mongo_file_id VARCHAR(100),
    attachment_filename VARCHAR(255),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(incident_id, checklist_id)
);

-- Create hotfix_checklist_progress table
CREATE TABLE IF NOT EXISTS hotfix_checklist_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hotfix_id UUID NOT NULL REFERENCES hotfixes(id) ON DELETE CASCADE,
    checklist_id UUID NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remark TEXT,
    mongo_file_id VARCHAR(100),
    attachment_filename VARCHAR(255),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(hotfix_id, checklist_id)
);

-- Add mongo_file_id to feature_checkpoint_progress for attachment support
ALTER TABLE feature_checkpoint_progress ADD COLUMN IF NOT EXISTS mongo_file_id VARCHAR(100);
ALTER TABLE feature_checkpoint_progress ADD COLUMN IF NOT EXISTS attachment_filename VARCHAR(255);

-- Insert default domains
INSERT INTO domains (name, description, color_code, is_active, created_by) VALUES
    ('KYC', 'Know Your Customer domain', '#722ed1', true, 'system'),
    ('Payments', 'Payment processing domain', '#1890ff', true, 'system'),
    ('Accounts', 'Account management domain', '#52c41a', true, 'system'),
    ('Loans', 'Loan management domain', '#faad14', true, 'system'),
    ('Cards', 'Card services domain', '#eb2f96', true, 'system'),
    ('General', 'General purpose domain', '#8c8c8c', true, 'system')
ON CONFLICT (name) DO NOTHING;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_incident_checklist_progress_incident ON incident_checklist_progress(incident_id);
CREATE INDEX IF NOT EXISTS idx_hotfix_checklist_progress_hotfix ON hotfix_checklist_progress(hotfix_id);
CREATE INDEX IF NOT EXISTS idx_domains_active ON domains(is_active);
