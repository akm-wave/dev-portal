-- V19: My Workspace Module - Personal Dev Notes and Smart Reminders

-- Create enum types
DO $$ BEGIN
    CREATE TYPE reminder_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE reminder_status AS ENUM ('PENDING', 'COMPLETED', 'OVERDUE', 'SNOOZED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- User Notes Table
CREATE TABLE IF NOT EXISTS user_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    tags TEXT[],
    is_pinned BOOLEAN DEFAULT FALSE,
    is_archived BOOLEAN DEFAULT FALSE,
    module_type VARCHAR(50),
    module_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Reminders Table
CREATE TABLE IF NOT EXISTS user_reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    reminder_datetime TIMESTAMP NOT NULL,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(20) DEFAULT 'PENDING',
    module_type VARCHAR(50),
    module_id UUID,
    is_system_generated BOOLEAN DEFAULT FALSE,
    snoozed_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for user_notes
CREATE INDEX IF NOT EXISTS idx_user_notes_user_id ON user_notes(user_id);
CREATE INDEX IF NOT EXISTS idx_user_notes_is_pinned ON user_notes(is_pinned);
CREATE INDEX IF NOT EXISTS idx_user_notes_is_archived ON user_notes(is_archived);
CREATE INDEX IF NOT EXISTS idx_user_notes_module ON user_notes(module_type, module_id);
CREATE INDEX IF NOT EXISTS idx_user_notes_created_at ON user_notes(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_notes_tags ON user_notes USING GIN(tags);

-- Indexes for user_reminders
CREATE INDEX IF NOT EXISTS idx_user_reminders_user_id ON user_reminders(user_id);
CREATE INDEX IF NOT EXISTS idx_user_reminders_datetime ON user_reminders(reminder_datetime);
CREATE INDEX IF NOT EXISTS idx_user_reminders_status ON user_reminders(status);
CREATE INDEX IF NOT EXISTS idx_user_reminders_priority ON user_reminders(priority);
CREATE INDEX IF NOT EXISTS idx_user_reminders_module ON user_reminders(module_type, module_id);
CREATE INDEX IF NOT EXISTS idx_user_reminders_system_generated ON user_reminders(is_system_generated);

-- Full-text search index for notes
CREATE INDEX IF NOT EXISTS idx_user_notes_search ON user_notes USING GIN(to_tsvector('english', title || ' ' || COALESCE(description, '')));
