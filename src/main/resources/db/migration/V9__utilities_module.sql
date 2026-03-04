-- V9: Utilities Module for MOP, CR, Guidelines, SOPs

-- Utility Type Enum values: MOP, CR_REQUIREMENT, DEVELOPMENT_GUIDELINE, SOP, OTHERS

-- Utilities table
CREATE TABLE IF NOT EXISTS utilities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(300) NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'OTHERS',
    description TEXT,
    version VARCHAR(50),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Utility Attachments table
CREATE TABLE IF NOT EXISTS utility_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    utility_id UUID NOT NULL REFERENCES utilities(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT,
    uploaded_by UUID REFERENCES users(id),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Utility Links table (optional linkage to other entities)
CREATE TABLE IF NOT EXISTS utility_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    utility_id UUID NOT NULL REFERENCES utilities(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_utilities_type ON utilities(type);
CREATE INDEX IF NOT EXISTS idx_utilities_title ON utilities(title);
CREATE INDEX IF NOT EXISTS idx_utilities_created_at ON utilities(created_at);
CREATE INDEX IF NOT EXISTS idx_utilities_created_by ON utilities(created_by);

CREATE INDEX IF NOT EXISTS idx_utility_attachments_utility ON utility_attachments(utility_id);
CREATE INDEX IF NOT EXISTS idx_utility_attachments_uploaded_by ON utility_attachments(uploaded_by);

CREATE INDEX IF NOT EXISTS idx_utility_links_utility ON utility_links(utility_id);
CREATE INDEX IF NOT EXISTS idx_utility_links_entity ON utility_links(entity_type, entity_id);

-- Full-text search index for utilities
CREATE INDEX IF NOT EXISTS idx_utilities_title_gin ON utilities USING gin(to_tsvector('english', title));
CREATE INDEX IF NOT EXISTS idx_utilities_description_gin ON utilities USING gin(to_tsvector('english', COALESCE(description, '')));
