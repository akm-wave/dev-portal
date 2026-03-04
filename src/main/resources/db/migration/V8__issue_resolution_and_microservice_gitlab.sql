-- V8: Issue Resolution Enhancement and Microservice GitLab URL

-- Add gitlab_url to microservices table
ALTER TABLE microservices ADD COLUMN IF NOT EXISTS gitlab_url VARCHAR(500);

-- Create issue_attachments table for multiple file uploads
CREATE TABLE IF NOT EXISTS issue_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    uploaded_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create issue_comments table for resolution comments
CREATE TABLE IF NOT EXISTS issue_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_id UUID NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    is_resolution_comment BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_issue_attachments_issue ON issue_attachments(issue_id);
CREATE INDEX IF NOT EXISTS idx_issue_attachments_uploaded_by ON issue_attachments(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_issue_comments_issue ON issue_comments(issue_id);
CREATE INDEX IF NOT EXISTS idx_issue_comments_user ON issue_comments(user_id);
CREATE INDEX IF NOT EXISTS idx_microservices_gitlab_url ON microservices(gitlab_url);

-- Full-text search indexes for global search
CREATE INDEX IF NOT EXISTS idx_features_name_gin ON features USING gin(to_tsvector('english', name));
CREATE INDEX IF NOT EXISTS idx_features_description_gin ON features USING gin(to_tsvector('english', COALESCE(description, '')));
CREATE INDEX IF NOT EXISTS idx_microservices_name_gin ON microservices USING gin(to_tsvector('english', name));
CREATE INDEX IF NOT EXISTS idx_microservices_description_gin ON microservices USING gin(to_tsvector('english', COALESCE(description, '')));
CREATE INDEX IF NOT EXISTS idx_checklists_name_gin ON checklists USING gin(to_tsvector('english', name));
CREATE INDEX IF NOT EXISTS idx_incidents_title_gin ON incidents USING gin(to_tsvector('english', title));
CREATE INDEX IF NOT EXISTS idx_hotfixes_title_gin ON hotfixes USING gin(to_tsvector('english', title));
CREATE INDEX IF NOT EXISTS idx_issues_title_gin ON issues USING gin(to_tsvector('english', title));
