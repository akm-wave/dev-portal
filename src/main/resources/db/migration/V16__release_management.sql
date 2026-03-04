-- Release Management Module
-- V16: Add tables for releases, release_microservices, and release_links

-- Release status enum type
CREATE TYPE release_status AS ENUM ('DRAFT', 'SCHEDULED', 'DEPLOYED', 'ROLLED_BACK');

-- Main releases table
CREATE TABLE releases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    version VARCHAR(100) NOT NULL,
    release_date TIMESTAMP,
    description TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Release to microservice mapping with metadata
CREATE TABLE release_microservices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    release_id UUID NOT NULL REFERENCES releases(id) ON DELETE CASCADE,
    microservice_id UUID NOT NULL REFERENCES microservices(id),
    branch_name VARCHAR(255),
    build_number VARCHAR(100),
    release_date TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(release_id, microservice_id)
);

-- Release links to Features, Incidents, Hotfixes, Issues
CREATE TABLE release_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    release_id UUID NOT NULL REFERENCES releases(id) ON DELETE CASCADE,
    entity_type VARCHAR(20) NOT NULL, -- FEATURE, INCIDENT, HOTFIX, ISSUE
    entity_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(release_id, entity_type, entity_id)
);

-- Indexes for better query performance
CREATE INDEX idx_releases_status ON releases(status);
CREATE INDEX idx_releases_version ON releases(version);
CREATE INDEX idx_releases_release_date ON releases(release_date);
CREATE INDEX idx_releases_created_by ON releases(created_by);
CREATE INDEX idx_release_microservices_release_id ON release_microservices(release_id);
CREATE INDEX idx_release_microservices_microservice_id ON release_microservices(microservice_id);
CREATE INDEX idx_release_links_release_id ON release_links(release_id);
CREATE INDEX idx_release_links_entity_type ON release_links(entity_type);
CREATE INDEX idx_release_links_entity_id ON release_links(entity_id);
