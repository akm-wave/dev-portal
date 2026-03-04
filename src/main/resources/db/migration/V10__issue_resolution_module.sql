-- V10: Issue Resolution Module with MongoDB file storage references

-- Issue Resolutions table (comments/entries in resolution timeline)
CREATE TABLE issue_resolutions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_id UUID NOT NULL,
    comment TEXT,
    is_resolution_comment BOOLEAN DEFAULT FALSE,
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_issue_resolution_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE,
    CONSTRAINT fk_issue_resolution_user FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Issue Resolution Attachments table (references MongoDB GridFS files)
CREATE TABLE issue_resolution_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_resolution_id UUID NOT NULL,
    mongo_file_id VARCHAR(100) NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    uploaded_by UUID,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resolution_attachment_resolution FOREIGN KEY (issue_resolution_id) REFERENCES issue_resolutions(id) ON DELETE CASCADE,
    CONSTRAINT fk_resolution_attachment_user FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

-- Indexes for issue_resolutions
CREATE INDEX idx_issue_resolutions_issue_id ON issue_resolutions(issue_id);
CREATE INDEX idx_issue_resolutions_created_at ON issue_resolutions(created_at DESC);
CREATE INDEX idx_issue_resolutions_created_by ON issue_resolutions(created_by);

-- Indexes for issue_resolution_attachments
CREATE INDEX idx_resolution_attachments_resolution_id ON issue_resolution_attachments(issue_resolution_id);
CREATE INDEX idx_resolution_attachments_mongo_file_id ON issue_resolution_attachments(mongo_file_id);

-- Update utility_attachments to use mongo_file_id instead of file_url
ALTER TABLE utility_attachments ADD COLUMN IF NOT EXISTS mongo_file_id VARCHAR(100);
ALTER TABLE utility_attachments ALTER COLUMN file_url DROP NOT NULL;

-- Create index for mongo_file_id in utility_attachments
CREATE INDEX IF NOT EXISTS idx_utility_attachments_mongo_file_id ON utility_attachments(mongo_file_id);
