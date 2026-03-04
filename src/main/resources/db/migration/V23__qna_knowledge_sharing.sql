-- V23: Q&A / Knowledge Sharing Module

-- Create qna_questions table
CREATE TABLE IF NOT EXISTS qna_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    tags TEXT,
    view_count INTEGER DEFAULT 0,
    upvotes INTEGER DEFAULT 0,
    is_resolved BOOLEAN DEFAULT FALSE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create qna_answers table
CREATE TABLE IF NOT EXISTS qna_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL REFERENCES qna_questions(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    upvotes INTEGER DEFAULT 0,
    is_accepted BOOLEAN DEFAULT FALSE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create qna_comments table
CREATE TABLE IF NOT EXISTS qna_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    answer_id UUID NOT NULL REFERENCES qna_answers(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create qna_attachments table
CREATE TABLE IF NOT EXISTS qna_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID REFERENCES qna_questions(id) ON DELETE CASCADE,
    answer_id UUID REFERENCES qna_answers(id) ON DELETE CASCADE,
    comment_id UUID REFERENCES qna_comments(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    mongo_file_id VARCHAR(100),
    file_url TEXT,
    uploaded_by VARCHAR(100),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_qna_attachment_parent CHECK (
        (question_id IS NOT NULL)::int + 
        (answer_id IS NOT NULL)::int + 
        (comment_id IS NOT NULL)::int = 1
    )
);

-- Create qna_hyperlinks table for storing links
CREATE TABLE IF NOT EXISTS qna_hyperlinks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID REFERENCES qna_questions(id) ON DELETE CASCADE,
    answer_id UUID REFERENCES qna_answers(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for efficient lookups
CREATE INDEX IF NOT EXISTS idx_qna_questions_created_by ON qna_questions(created_by);
CREATE INDEX IF NOT EXISTS idx_qna_questions_created_at ON qna_questions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_qna_questions_resolved ON qna_questions(is_resolved);

CREATE INDEX IF NOT EXISTS idx_qna_answers_question ON qna_answers(question_id);
CREATE INDEX IF NOT EXISTS idx_qna_answers_created_by ON qna_answers(created_by);
CREATE INDEX IF NOT EXISTS idx_qna_answers_accepted ON qna_answers(is_accepted);

CREATE INDEX IF NOT EXISTS idx_qna_comments_answer ON qna_comments(answer_id);

CREATE INDEX IF NOT EXISTS idx_qna_attachments_question ON qna_attachments(question_id);
CREATE INDEX IF NOT EXISTS idx_qna_attachments_answer ON qna_attachments(answer_id);

-- Full-text search indexes
CREATE INDEX IF NOT EXISTS idx_qna_questions_title_fts ON qna_questions USING GIN (to_tsvector('english', title));
CREATE INDEX IF NOT EXISTS idx_qna_questions_content_fts ON qna_questions USING GIN (to_tsvector('english', content));
CREATE INDEX IF NOT EXISTS idx_qna_answers_content_fts ON qna_answers USING GIN (to_tsvector('english', content));
CREATE INDEX IF NOT EXISTS idx_qna_comments_content_fts ON qna_comments USING GIN (to_tsvector('english', content));

-- User stats table for gamification
CREATE TABLE IF NOT EXISTS qna_user_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    questions_asked INTEGER DEFAULT 0,
    answers_given INTEGER DEFAULT 0,
    accepted_answers INTEGER DEFAULT 0,
    total_upvotes INTEGER DEFAULT 0,
    reputation_score INTEGER DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
