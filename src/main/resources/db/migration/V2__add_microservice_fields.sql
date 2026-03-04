-- Add highRisk and technicalDebtScore columns to microservices table
ALTER TABLE microservices ADD COLUMN IF NOT EXISTS high_risk BOOLEAN DEFAULT FALSE;
ALTER TABLE microservices ADD COLUMN IF NOT EXISTS technical_debt_score INTEGER DEFAULT 0;
