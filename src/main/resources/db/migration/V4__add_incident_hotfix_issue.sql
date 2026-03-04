-- Incidents table
CREATE TABLE IF NOT EXISTS incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    main_feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    owner_id UUID REFERENCES users(id),
    created_by VARCHAR(100),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Incident-Microservices join table
CREATE TABLE IF NOT EXISTS incident_microservices (
    incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    microservice_id UUID NOT NULL REFERENCES microservices(id) ON DELETE CASCADE,
    PRIMARY KEY (incident_id, microservice_id)
);

-- Hotfixes table
CREATE TABLE IF NOT EXISTS hotfixes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    release_version VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    main_feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    owner_id UUID REFERENCES users(id),
    created_by VARCHAR(100),
    deployed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Hotfix-Microservices join table
CREATE TABLE IF NOT EXISTS hotfix_microservices (
    hotfix_id UUID NOT NULL REFERENCES hotfixes(id) ON DELETE CASCADE,
    microservice_id UUID NOT NULL REFERENCES microservices(id) ON DELETE CASCADE,
    PRIMARY KEY (hotfix_id, microservice_id)
);

-- Issues table
CREATE TABLE IF NOT EXISTS issues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    main_feature_id UUID NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    assigned_to UUID REFERENCES users(id),
    owner_id UUID REFERENCES users(id),
    created_by VARCHAR(100),
    result_comment TEXT,
    attachment_url VARCHAR(500),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_incidents_main_feature ON incidents(main_feature_id);
CREATE INDEX IF NOT EXISTS idx_incidents_owner ON incidents(owner_id);
CREATE INDEX IF NOT EXISTS idx_incidents_status ON incidents(status);
CREATE INDEX IF NOT EXISTS idx_incidents_severity ON incidents(severity);

CREATE INDEX IF NOT EXISTS idx_hotfixes_main_feature ON hotfixes(main_feature_id);
CREATE INDEX IF NOT EXISTS idx_hotfixes_owner ON hotfixes(owner_id);
CREATE INDEX IF NOT EXISTS idx_hotfixes_status ON hotfixes(status);

CREATE INDEX IF NOT EXISTS idx_issues_main_feature ON issues(main_feature_id);
CREATE INDEX IF NOT EXISTS idx_issues_assigned_to ON issues(assigned_to);
CREATE INDEX IF NOT EXISTS idx_issues_owner ON issues(owner_id);
CREATE INDEX IF NOT EXISTS idx_issues_status ON issues(status);
