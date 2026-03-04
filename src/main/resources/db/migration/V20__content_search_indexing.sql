-- V20: Content-based search indexing for attachments

-- Add content indexing columns to utility_attachments
ALTER TABLE utility_attachments 
ADD COLUMN IF NOT EXISTS content_index TEXT,
ADD COLUMN IF NOT EXISTS extraction_status VARCHAR(20) DEFAULT 'PENDING',
ADD COLUMN IF NOT EXISTS indexed_at TIMESTAMP;

-- Add content indexing columns to issue_attachments
ALTER TABLE issue_attachments 
ADD COLUMN IF NOT EXISTS content_index TEXT,
ADD COLUMN IF NOT EXISTS extraction_status VARCHAR(20) DEFAULT 'PENDING',
ADD COLUMN IF NOT EXISTS indexed_at TIMESTAMP;

-- Add content indexing columns to issue_resolution_attachments
ALTER TABLE issue_resolution_attachments 
ADD COLUMN IF NOT EXISTS content_index TEXT,
ADD COLUMN IF NOT EXISTS extraction_status VARCHAR(20) DEFAULT 'PENDING',
ADD COLUMN IF NOT EXISTS indexed_at TIMESTAMP;

-- Create indexes for extraction status
CREATE INDEX IF NOT EXISTS idx_utility_attachments_extraction_status ON utility_attachments(extraction_status);
CREATE INDEX IF NOT EXISTS idx_issue_attachments_extraction_status ON issue_attachments(extraction_status);
CREATE INDEX IF NOT EXISTS idx_issue_resolution_attachments_extraction_status ON issue_resolution_attachments(extraction_status);

-- Create Full-Text Search indexes for content search
CREATE INDEX IF NOT EXISTS idx_utility_attachments_content_search 
ON utility_attachments 
USING GIN (to_tsvector('english', COALESCE(content_index, '') || ' ' || COALESCE(file_name, '')));

CREATE INDEX IF NOT EXISTS idx_issue_attachments_content_search 
ON issue_attachments 
USING GIN (to_tsvector('english', COALESCE(content_index, '') || ' ' || COALESCE(file_name, '')));

CREATE INDEX IF NOT EXISTS idx_issue_resolution_attachments_content_search 
ON issue_resolution_attachments 
USING GIN (to_tsvector('english', COALESCE(content_index, '') || ' ' || COALESCE(file_name, '')));

-- Create Full-Text Search index for utilities content
CREATE INDEX IF NOT EXISTS idx_utilities_content_search 
ON utilities 
USING GIN (to_tsvector('english', COALESCE(title, '') || ' ' || COALESCE(description, '') || ' ' || COALESCE(content, '')));

-- Update existing attachments to mark them for indexing
UPDATE utility_attachments SET extraction_status = 'PENDING' WHERE extraction_status IS NULL;
UPDATE issue_attachments SET extraction_status = 'PENDING' WHERE extraction_status IS NULL;
UPDATE issue_resolution_attachments SET extraction_status = 'PENDING' WHERE extraction_status IS NULL;
