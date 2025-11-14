-- ============================================
-- CREATE TABLE IF NOT EXISTS
-- ============================================
CREATE TABLE IF NOT EXISTS atsdata (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    job_id UUID NOT NULL,
    ats_score NUMERIC(5,2) NOT NULL,
    created_at DATE NOT NULL DEFAULT CURRENT_DATE,
    updated_at DATE NOT NULL DEFAULT CURRENT_DATE
);

-- ============================================
-- ALTER COLUMNS TO MATCH ENTITY
-- ============================================

-- Ensure user_id is UUID and NOT NULL
ALTER TABLE atsdata
    ALTER COLUMN user_id TYPE UUID USING user_id::uuid,
    ALTER COLUMN user_id SET NOT NULL;

-- Ensure job_id is UUID and NOT NULL
ALTER TABLE atsdata
    ALTER COLUMN job_id TYPE UUID USING job_id::uuid,
    ALTER COLUMN job_id SET NOT NULL;

-- Ensure ats_score type
ALTER TABLE atsdata
    ALTER COLUMN ats_score TYPE NUMERIC(5,2),
    ALTER COLUMN ats_score SET NOT NULL;

-- Ensure created_at and updated_at exist and correct type
ALTER TABLE atsdata
    ALTER COLUMN created_at TYPE DATE,
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE atsdata
    ALTER COLUMN updated_at TYPE DATE,
    ALTER COLUMN updated_at SET NOT NULL;

-- ============================================
-- ADD UNIQUE CONSTRAINT (user_id + job_id)
-- ============================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'unique_user_job'
    ) THEN
        ALTER TABLE atsdata
        ADD CONSTRAINT unique_user_job UNIQUE (user_id, job_id);
    END IF;
END $$;


-- ============================================================
-- CREATE TABLE IF NOT EXISTS
-- ============================================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    plan_type VARCHAR(255) NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    amount INT,
    currency VARCHAR(50),
    status VARCHAR(100),
    created_at DATE NOT NULL DEFAULT CURRENT_DATE,
    updated_at DATE NOT NULL DEFAULT CURRENT_DATE
);

-- ============================================================
-- ALTER COLUMNS TO MATCH ENTITY DEFINITION
-- ============================================================

-- Ensure user_id is UUID
ALTER TABLE payments
    ALTER COLUMN user_id TYPE UUID USING user_id::uuid,
    ALTER COLUMN user_id SET NOT NULL;

-- plan_type VARCHAR(255) NOT NULL
ALTER TABLE payments
    ALTER COLUMN plan_type TYPE VARCHAR(255),
    ALTER COLUMN plan_type SET NOT NULL;

-- session_id VARCHAR(255) NOT NULL
ALTER TABLE payments
    ALTER COLUMN session_id TYPE VARCHAR(255),
    ALTER COLUMN session_id SET NOT NULL;

-- amount is integer
ALTER TABLE payments
    ALTER COLUMN amount TYPE INT;

-- currency VARCHAR(50)
ALTER TABLE payments
    ALTER COLUMN currency TYPE VARCHAR(50);

-- status VARCHAR(100)
ALTER TABLE payments
    ALTER COLUMN status TYPE VARCHAR(100);

-- created_at + updated_at should be DATE and NOT NULL
ALTER TABLE payments
    ALTER COLUMN created_at TYPE DATE,
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE payments
    ALTER COLUMN updated_at TYPE DATE,
    ALTER COLUMN updated_at SET NOT NULL;


-- ======================================================================
-- CREATE TABLE IF NOT EXISTS
-- ======================================================================
CREATE TABLE IF NOT EXISTS user_resume_plans (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    plan_type VARCHAR(255) NOT NULL,
    payment_ids INT[],

    creations_remaining INT NOT NULL,
    enhancements_remaining INT NOT NULL,
    downloads_remaining INT NOT NULL,

    resumes JSONB,

    created_at DATE NOT NULL DEFAULT CURRENT_DATE,
    updated_at DATE NOT NULL DEFAULT CURRENT_DATE
);

-- ======================================================================
-- ALTER COLUMNS – ENSURE TYPES & CONSTRAINTS MATCH ENTITY
-- ======================================================================

-- user_id as UUID NOT NULL
ALTER TABLE user_resume_plans
    ALTER COLUMN user_id TYPE UUID USING user_id::uuid,
    ALTER COLUMN user_id SET NOT NULL;

-- plan_type VARCHAR NOT NULL
ALTER TABLE user_resume_plans
    ALTER COLUMN plan_type TYPE VARCHAR(255),
    ALTER COLUMN plan_type SET NOT NULL;

-- payment_ids as INT[]
ALTER TABLE user_resume_plans
    ALTER COLUMN payment_ids TYPE INT[];

-- creations_remaining INT NOT NULL
ALTER TABLE user_resume_plans
    ALTER COLUMN creations_remaining TYPE INT,
    ALTER COLUMN creations_remaining SET NOT NULL;

-- enhancements_remaining INT NOT NULL
ALTER TABLE user_resume_plans
    ALTER COLUMN enhancements_remaining TYPE INT,
    ALTER COLUMN enhancements_remaining SET NOT NULL;

-- downloads_remaining INT NOT NULL
ALTER TABLE user_resume_plans
    ALTER COLUMN downloads_remaining TYPE INT,
    ALTER COLUMN downloads_remaining SET NOT NULL;

-- resumes JSONB
ALTER TABLE user_resume_plans
    ALTER COLUMN resumes TYPE JSONB USING resumes::jsonb;

-- created_at DATE NOT NULL
ALTER TABLE user_resume_plans
    ALTER COLUMN created_at TYPE DATE,
    ALTER COLUMN created_at SET NOT NULL;

-- updated_at DATE NOT NULL
ALTER TABLE user_resume_plans
    ALTER COLUMN updated_at TYPE DATE,
    ALTER COLUMN updated_at SET NOT NULL;
