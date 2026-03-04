-- V18: Add old build number and feature branch to releases

ALTER TABLE releases ADD COLUMN IF NOT EXISTS old_build_number VARCHAR(100);
ALTER TABLE releases ADD COLUMN IF NOT EXISTS feature_branch VARCHAR(255);

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_releases_old_build_number ON releases(old_build_number);
CREATE INDEX IF NOT EXISTS idx_releases_feature_branch ON releases(feature_branch);
