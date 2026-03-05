-- V26: Standardize Status Enums Across All Entities
-- This migration updates all status values to follow a consistent pattern:
-- PLANNED → IN_PROGRESS → COMPLETED → [DEPLOYED/RELEASED/CLOSED]

-- 1. Update Incidents: OPEN → PLANNED, RESOLVED → COMPLETED
UPDATE incidents SET status = 'PLANNED' WHERE status = 'OPEN';
UPDATE incidents SET status = 'COMPLETED' WHERE status = 'RESOLVED';

-- 2. Update Issues: OPEN → PLANNED, RESOLVED → COMPLETED
UPDATE issues SET status = 'PLANNED' WHERE status = 'OPEN';
UPDATE issues SET status = 'COMPLETED' WHERE status = 'RESOLVED';

-- 3. Update Microservices: NOT_STARTED → PLANNED
UPDATE microservices SET status = 'PLANNED' WHERE status = 'NOT_STARTED';

-- 4. Update Checklists: PENDING → PLANNED, DONE → COMPLETED
UPDATE checklists SET status = 'PLANNED' WHERE status = 'PENDING';
UPDATE checklists SET status = 'COMPLETED' WHERE status = 'DONE';

-- 5. Update Feature Checkpoints: PENDING → PLANNED, DONE → COMPLETED
UPDATE feature_checkpoints SET status = 'PLANNED' WHERE status = 'PENDING';
UPDATE feature_checkpoints SET status = 'COMPLETED' WHERE status = 'DONE';

-- 6. Update Incident Checklist Progress: PENDING → PLANNED, DONE → COMPLETED
UPDATE incident_checklist_progress SET status = 'PLANNED' WHERE status = 'PENDING';
UPDATE incident_checklist_progress SET status = 'COMPLETED' WHERE status = 'DONE';

-- 7. Update Hotfix Checklist Progress: PENDING → PLANNED, DONE → COMPLETED
UPDATE hotfix_checklist_progress SET status = 'PLANNED' WHERE status = 'PENDING';
UPDATE hotfix_checklist_progress SET status = 'COMPLETED' WHERE status = 'DONE';

-- Add comments for documentation
COMMENT ON COLUMN incidents.status IS 'Status: PLANNED, IN_PROGRESS, COMPLETED, CLOSED';
COMMENT ON COLUMN issues.status IS 'Status: PLANNED, ASSIGNED, IN_PROGRESS, COMPLETED, CLOSED';
COMMENT ON COLUMN hotfixes.status IS 'Status: PLANNED, IN_PROGRESS, COMPLETED, DEPLOYED';
COMMENT ON COLUMN features.status IS 'Status: PLANNED, IN_PROGRESS, COMPLETED, RELEASED';
COMMENT ON COLUMN microservices.status IS 'Status: PLANNED, IN_PROGRESS, COMPLETED';
COMMENT ON COLUMN checklists.status IS 'Status: PLANNED, IN_PROGRESS, COMPLETED, BLOCKED';
COMMENT ON COLUMN feature_checkpoints.status IS 'Status: PLANNED, IN_PROGRESS, COMPLETED, BLOCKED';
