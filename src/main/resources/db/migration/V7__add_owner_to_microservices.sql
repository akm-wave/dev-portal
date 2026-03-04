-- Add owner_id column to microservices table (if not exists from V6)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'microservices' AND column_name = 'owner_id') THEN
        ALTER TABLE microservices ADD COLUMN owner_id UUID REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Rename assigned_to_id to owner_id in features table if it exists
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'features' AND column_name = 'assigned_to_id') THEN
        ALTER TABLE features RENAME COLUMN assigned_to_id TO owner_id;
    END IF;
END $$;

-- Add owner_id to features if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'features' AND column_name = 'owner_id') THEN
        ALTER TABLE features ADD COLUMN owner_id UUID REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_features_owner ON features(owner_id);
CREATE INDEX IF NOT EXISTS idx_microservices_owner ON microservices(owner_id);
