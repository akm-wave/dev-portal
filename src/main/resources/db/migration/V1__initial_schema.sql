-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Checklists table (base entity)
CREATE TABLE checklists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    created_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Microservices table
CREATE TABLE microservices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    version VARCHAR(50),
    owner VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Features table
CREATE TABLE features (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    release_version VARCHAR(50),
    target_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Join table: microservice_checklists
CREATE TABLE microservice_checklists (
    microservice_id UUID NOT NULL,
    checklist_id UUID NOT NULL,
    PRIMARY KEY (microservice_id, checklist_id),
    FOREIGN KEY (microservice_id) REFERENCES microservices(id) ON DELETE CASCADE,
    FOREIGN KEY (checklist_id) REFERENCES checklists(id) ON DELETE CASCADE
);

-- Join table: feature_microservices
CREATE TABLE feature_microservices (
    feature_id UUID NOT NULL,
    microservice_id UUID NOT NULL,
    PRIMARY KEY (feature_id, microservice_id),
    FOREIGN KEY (feature_id) REFERENCES features(id) ON DELETE CASCADE,
    FOREIGN KEY (microservice_id) REFERENCES microservices(id) ON DELETE CASCADE
);

-- Audit trail table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Activity log table
CREATE TABLE activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id UUID,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for performance
CREATE INDEX idx_checklists_status ON checklists(status);
CREATE INDEX idx_checklists_priority ON checklists(priority);
CREATE INDEX idx_checklists_is_active ON checklists(is_active);
CREATE INDEX idx_checklists_created_at ON checklists(created_at);

CREATE INDEX idx_microservices_status ON microservices(status);
CREATE INDEX idx_microservices_created_at ON microservices(created_at);

CREATE INDEX idx_features_status ON features(status);
CREATE INDEX idx_features_target_date ON features(target_date);
CREATE INDEX idx_features_created_at ON features(created_at);

CREATE INDEX idx_microservice_checklists_microservice ON microservice_checklists(microservice_id);
CREATE INDEX idx_microservice_checklists_checklist ON microservice_checklists(checklist_id);

CREATE INDEX idx_feature_microservices_feature ON feature_microservices(feature_id);
CREATE INDEX idx_feature_microservices_microservice ON feature_microservices(microservice_id);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_changed_at ON audit_logs(changed_at);

CREATE INDEX idx_activity_logs_user ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at);

-- Insert default admin user (password: Admin@123 encrypted with BCrypt)
INSERT INTO users (id, username, email, password, role, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@devportal.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkHx7zaGW9SFrfH5VLjLK3K.2',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
