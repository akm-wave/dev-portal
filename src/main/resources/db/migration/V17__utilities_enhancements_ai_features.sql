-- V17: Utilities Enhancements & AI Features

-- ============================================
-- 1. VERSIONED DOCUMENTATION
-- ============================================

-- Utility versions table for version history
CREATE TABLE IF NOT EXISTS utility_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    utility_id UUID NOT NULL REFERENCES utilities(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    content TEXT,
    change_summary VARCHAR(500),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_current BOOLEAN DEFAULT FALSE,
    UNIQUE(utility_id, version_number)
);

-- Add content field to utilities for rich text content
ALTER TABLE utilities ADD COLUMN IF NOT EXISTS content TEXT;

-- Add current_version to utilities
ALTER TABLE utilities ADD COLUMN IF NOT EXISTS current_version INT DEFAULT 1;

-- ============================================
-- 2. TEMPLATE MANAGEMENT
-- ============================================

-- Templates table for reusable templates
CREATE TABLE IF NOT EXISTS templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    entity_type VARCHAR(50) NOT NULL, -- UTILITY, ISSUE, RELEASE
    template_data JSONB NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 3. SEARCHABLE KNOWLEDGE BASE (Tags & Categories)
-- ============================================

-- Tags table
CREATE TABLE IF NOT EXISTS tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(20) DEFAULT '#5b6cf0',
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories table for organizing utilities
CREATE TABLE IF NOT EXISTS utility_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    parent_id UUID REFERENCES utility_categories(id),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Utility-Tags junction table
CREATE TABLE IF NOT EXISTS utility_tags (
    utility_id UUID NOT NULL REFERENCES utilities(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (utility_id, tag_id)
);

-- Add category to utilities
ALTER TABLE utilities ADD COLUMN IF NOT EXISTS category_id UUID REFERENCES utility_categories(id);

-- ============================================
-- 4. AI FEATURES - Smart Summaries
-- ============================================

-- AI generated summaries table
CREATE TABLE IF NOT EXISTS ai_summaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL, -- ISSUE, RELEASE, INCIDENT, HOTFIX
    entity_id UUID NOT NULL,
    summary_type VARCHAR(50) NOT NULL, -- THREAD_SUMMARY, RELEASE_NOTES, RESOLUTION_SUMMARY
    summary_text TEXT NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    generated_by VARCHAR(100) DEFAULT 'SYSTEM',
    is_approved BOOLEAN DEFAULT FALSE,
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP
);

-- ============================================
-- 5. DUPLICATE DETECTION
-- ============================================

-- Similar items suggestions table (cached suggestions)
CREATE TABLE IF NOT EXISTS similarity_suggestions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_entity_type VARCHAR(50) NOT NULL,
    source_entity_id UUID NOT NULL,
    similar_entity_type VARCHAR(50) NOT NULL,
    similar_entity_id UUID NOT NULL,
    similarity_score DECIMAL(5,4) NOT NULL,
    suggestion_reason VARCHAR(255),
    is_dismissed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(source_entity_type, source_entity_id, similar_entity_type, similar_entity_id)
);

-- ============================================
-- 6. RECOMMENDATION ENGINE
-- ============================================

-- Release recommendations table
CREATE TABLE IF NOT EXISTS release_recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    release_id UUID NOT NULL REFERENCES releases(id) ON DELETE CASCADE,
    recommended_entity_type VARCHAR(50) NOT NULL, -- MICROSERVICE, ISSUE, HOTFIX, FEATURE
    recommended_entity_id UUID NOT NULL,
    recommendation_score DECIMAL(5,4) NOT NULL,
    recommendation_reason VARCHAR(255),
    is_accepted BOOLEAN DEFAULT FALSE,
    is_dismissed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX IF NOT EXISTS idx_utility_versions_utility ON utility_versions(utility_id);
CREATE INDEX IF NOT EXISTS idx_utility_versions_current ON utility_versions(utility_id, is_current);

CREATE INDEX IF NOT EXISTS idx_templates_entity_type ON templates(entity_type);
CREATE INDEX IF NOT EXISTS idx_templates_active ON templates(is_active);

CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name);
CREATE INDEX IF NOT EXISTS idx_utility_tags_utility ON utility_tags(utility_id);
CREATE INDEX IF NOT EXISTS idx_utility_tags_tag ON utility_tags(tag_id);

CREATE INDEX IF NOT EXISTS idx_utility_categories_parent ON utility_categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_utilities_category ON utilities(category_id);

CREATE INDEX IF NOT EXISTS idx_ai_summaries_entity ON ai_summaries(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_ai_summaries_type ON ai_summaries(summary_type);

CREATE INDEX IF NOT EXISTS idx_similarity_source ON similarity_suggestions(source_entity_type, source_entity_id);
CREATE INDEX IF NOT EXISTS idx_similarity_similar ON similarity_suggestions(similar_entity_type, similar_entity_id);

CREATE INDEX IF NOT EXISTS idx_release_recommendations_release ON release_recommendations(release_id);
CREATE INDEX IF NOT EXISTS idx_release_recommendations_entity ON release_recommendations(recommended_entity_type, recommended_entity_id);

-- Full-text search on utility content
CREATE INDEX IF NOT EXISTS idx_utilities_content_gin ON utilities USING gin(to_tsvector('english', COALESCE(content, '')));

-- Insert default categories
INSERT INTO utility_categories (name, description, sort_order) VALUES
    ('MOPs & Procedures', 'Method of Procedures and operational guides', 1),
    ('Development Guidelines', 'Coding standards and development practices', 2),
    ('CR Requirements', 'Change Request documentation', 3),
    ('Lessons Learned', 'Postmortem and retrospective documentation', 4),
    ('Internal References', 'Internal documentation and references', 5)
ON CONFLICT (name) DO NOTHING;

-- Insert default tags
INSERT INTO tags (name, color, description) VALUES
    ('critical', '#ef4444', 'Critical priority items'),
    ('production', '#f59e0b', 'Production-related documentation'),
    ('development', '#10b981', 'Development-related documentation'),
    ('security', '#8b5cf6', 'Security-related documentation'),
    ('infrastructure', '#3b82f6', 'Infrastructure documentation'),
    ('database', '#06b6d4', 'Database-related documentation'),
    ('api', '#ec4899', 'API documentation'),
    ('deployment', '#84cc16', 'Deployment procedures')
ON CONFLICT (name) DO NOTHING;
