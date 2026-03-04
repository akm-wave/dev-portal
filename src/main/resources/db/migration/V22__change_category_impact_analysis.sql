-- V22: Change Category and Impact Analysis System

-- Create change_type enum values will be stored as VARCHAR
-- Change types: CODE_CHANGE, CONFIG_CHANGE, DB_CHANGE, API_CHANGE, INFRA_CHANGE

-- Create microservice_change_category table
CREATE TABLE IF NOT EXISTS microservice_change_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    microservice_id UUID NOT NULL REFERENCES microservices(id) ON DELETE CASCADE,
    feature_id UUID REFERENCES features(id) ON DELETE CASCADE,
    incident_id UUID REFERENCES incidents(id) ON DELETE CASCADE,
    hotfix_id UUID REFERENCES hotfixes(id) ON DELETE CASCADE,
    issue_id UUID REFERENCES issues(id) ON DELETE CASCADE,
    change_type VARCHAR(50) NOT NULL,
    notes TEXT,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_one_parent CHECK (
        (feature_id IS NOT NULL)::int + 
        (incident_id IS NOT NULL)::int + 
        (hotfix_id IS NOT NULL)::int + 
        (issue_id IS NOT NULL)::int = 1
    )
);

-- Create impact_analysis table
CREATE TABLE IF NOT EXISTS impact_analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feature_id UUID REFERENCES features(id) ON DELETE CASCADE,
    incident_id UUID REFERENCES incidents(id) ON DELETE CASCADE,
    hotfix_id UUID REFERENCES hotfixes(id) ON DELETE CASCADE,
    issue_id UUID REFERENCES issues(id) ON DELETE CASCADE,
    risk_score INTEGER NOT NULL DEFAULT 0,
    risk_level VARCHAR(20) NOT NULL DEFAULT 'LOW',
    impacted_areas TEXT,
    impacted_microservices TEXT,
    critical_checklists TEXT,
    recommended_tests TEXT,
    analysis_summary TEXT,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_impact_one_parent CHECK (
        (feature_id IS NOT NULL)::int + 
        (incident_id IS NOT NULL)::int + 
        (hotfix_id IS NOT NULL)::int + 
        (issue_id IS NOT NULL)::int = 1
    )
);

-- Create indexes for efficient lookups
CREATE INDEX IF NOT EXISTS idx_change_cat_microservice ON microservice_change_categories(microservice_id);
CREATE INDEX IF NOT EXISTS idx_change_cat_feature ON microservice_change_categories(feature_id);
CREATE INDEX IF NOT EXISTS idx_change_cat_incident ON microservice_change_categories(incident_id);
CREATE INDEX IF NOT EXISTS idx_change_cat_hotfix ON microservice_change_categories(hotfix_id);
CREATE INDEX IF NOT EXISTS idx_change_cat_issue ON microservice_change_categories(issue_id);
CREATE INDEX IF NOT EXISTS idx_change_cat_type ON microservice_change_categories(change_type);

CREATE INDEX IF NOT EXISTS idx_impact_feature ON impact_analyses(feature_id);
CREATE INDEX IF NOT EXISTS idx_impact_incident ON impact_analyses(incident_id);
CREATE INDEX IF NOT EXISTS idx_impact_hotfix ON impact_analyses(hotfix_id);
CREATE INDEX IF NOT EXISTS idx_impact_issue ON impact_analyses(issue_id);

-- Assign some checklists to admin user for progress bar demo
-- First get admin user ID and assign checklists
DO $$
DECLARE
    admin_id UUID;
    checklist_ids UUID[];
    i INTEGER;
BEGIN
    -- Get admin user ID
    SELECT id INTO admin_id FROM users WHERE username = 'admin' LIMIT 1;
    
    IF admin_id IS NOT NULL THEN
        -- Get first 10 checklists
        SELECT ARRAY_AGG(id) INTO checklist_ids 
        FROM (SELECT id FROM checklists WHERE is_active = true ORDER BY created_at LIMIT 10) sub;
        
        IF checklist_ids IS NOT NULL THEN
            -- Assign checklists to admin with varying statuses and weights
            FOR i IN 1..array_length(checklist_ids, 1) LOOP
                UPDATE checklists 
                SET assigned_to = admin_id,
                    weight = CASE 
                        WHEN i <= 3 THEN 1
                        WHEN i <= 6 THEN 2
                        ELSE 3
                    END,
                    status = CASE 
                        WHEN i <= 4 THEN 'DONE'
                        WHEN i <= 6 THEN 'IN_PROGRESS'
                        WHEN i = 7 THEN 'BLOCKED'
                        ELSE 'PENDING'
                    END
                WHERE id = checklist_ids[i];
            END LOOP;
        END IF;
    END IF;
END $$;
