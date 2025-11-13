-- ============================================================
-- ✅ Migration: Add missing fields & fix structure for Employees, Users, Jobs
-- ============================================================

-- ==========================
-- 🧩 EMPLOYEES TABLE UPDATE
-- ==========================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'employees') THEN
        CREATE TABLE employees (
            id BIGSERIAL PRIMARY KEY
        );
    END IF;
END $$;

-- Add columns safely
ALTER TABLE employees ADD COLUMN IF NOT EXISTS emp_id VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS verified BOOLEAN DEFAULT FALSE;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS name VARCHAR(50);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS password VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS mobile VARCHAR(15);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS gender VARCHAR(10);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS gst_number VARCHAR(255);
ALTER TABLE employees ADD COLUMN IF NOT EXISTS profile_picture TEXT;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS role VARCHAR(50) DEFAULT 'EMPLOYER';
ALTER TABLE employees ADD COLUMN IF NOT EXISTS agreed_to_terms BOOLEAN DEFAULT FALSE;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE employees ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();

-- Drop legacy columns (do this before constraints to avoid conflicts)
ALTER TABLE employees DROP COLUMN IF EXISTS aadhar_number;
ALTER TABLE employees DROP COLUMN IF EXISTS pan_number;

-- Constraints
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'employees_emp_id_key') THEN
        ALTER TABLE employees ADD CONSTRAINT employees_emp_id_key UNIQUE (emp_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'employees_email_key') THEN
        ALTER TABLE employees ADD CONSTRAINT employees_email_key UNIQUE (email);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'employees_gst_number_key') THEN
        ALTER TABLE employees ADD CONSTRAINT employees_gst_number_key UNIQUE (gst_number);
    END IF;
END $$;

-- Safe NOT NULL enforcement (only after existing rows are populated)
UPDATE employees SET emp_id = COALESCE(emp_id, gen_random_uuid()::text);
UPDATE employees SET name = COALESCE(name, 'Unknown');
UPDATE employees SET email = COALESCE(email, CONCAT('unknown_', id, '@temp.com'));
UPDATE employees SET password = COALESCE(password, 'changeme');
UPDATE employees SET mobile = COALESCE(mobile, '0000000000');
UPDATE employees SET address = COALESCE(address, 'Not Provided');
UPDATE employees SET gender = COALESCE(gender, 'Other');
UPDATE employees SET date_of_birth = COALESCE(date_of_birth, CURRENT_DATE);
UPDATE employees SET gst_number = COALESCE(gst_number, CONCAT('GST', id));

ALTER TABLE employees ALTER COLUMN emp_id SET NOT NULL;
ALTER TABLE employees ALTER COLUMN verified SET NOT NULL;
ALTER TABLE employees ALTER COLUMN name SET NOT NULL;
ALTER TABLE employees ALTER COLUMN email SET NOT NULL;
ALTER TABLE employees ALTER COLUMN password SET NOT NULL;
ALTER TABLE employees ALTER COLUMN mobile SET NOT NULL;
ALTER TABLE employees ALTER COLUMN address SET NOT NULL;
ALTER TABLE employees ALTER COLUMN gender SET NOT NULL;
ALTER TABLE employees ALTER COLUMN date_of_birth SET NOT NULL;
ALTER TABLE employees ALTER COLUMN gst_number SET NOT NULL;
ALTER TABLE employees ALTER COLUMN role SET NOT NULL;
ALTER TABLE employees ALTER COLUMN agreed_to_terms SET NOT NULL;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_employees_emp_id ON employees(emp_id);
CREATE INDEX IF NOT EXISTS idx_employees_email ON employees(email);


-- ==========================
-- 🧍 USERS TABLE UPDATE
-- ==========================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        CREATE TABLE users (
            id BIGSERIAL PRIMARY KEY
        );
    END IF;
END $$;

ALTER TABLE users ADD COLUMN IF NOT EXISTS user_id VARCHAR(255) NOT NULL UNIQUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS mobile VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS gender VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS qualification VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS department VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS passout_year VARCHAR(10);
ALTER TABLE users ADD COLUMN IF NOT EXISTS dob VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS experience VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS linkedin VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS github VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS skills TEXT[];
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(50) DEFAULT 'USER';
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_picture TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS resume BYTEA;
ALTER TABLE users ADD COLUMN IF NOT EXISTS resume_content_type VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS resume_file_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS token_expiry_date TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);


-- ==========================
-- 💼 JOBS TABLE UPDATE
-- ==========================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'job') THEN
        CREATE TABLE job (
            id BIGSERIAL PRIMARY KEY
        );
    END IF;
END $$;

ALTER TABLE job ADD COLUMN IF NOT EXISTS job_id VARCHAR(255) NOT NULL UNIQUE;
ALTER TABLE job ADD COLUMN IF NOT EXISTS short_id VARCHAR(255) NOT NULL UNIQUE;
ALTER TABLE job ADD COLUMN IF NOT EXISTS title VARCHAR(255);
ALTER TABLE job ADD COLUMN IF NOT EXISTS company VARCHAR(255);
ALTER TABLE job ADD COLUMN IF NOT EXISTS location VARCHAR(255);
ALTER TABLE job ADD COLUMN IF NOT EXISTS highest_qualification VARCHAR(255);
ALTER TABLE job ADD COLUMN IF NOT EXISTS min_salary VARCHAR(100);
ALTER TABLE job ADD COLUMN IF NOT EXISTS max_salary VARCHAR(100);
ALTER TABLE job ADD COLUMN IF NOT EXISTS experience VARCHAR(100);
ALTER TABLE job ADD COLUMN IF NOT EXISTS job_type VARCHAR(100);
ALTER TABLE job ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE job ADD COLUMN IF NOT EXISTS contact_email VARCHAR(255);
ALTER TABLE job ADD COLUMN IF NOT EXISTS department VARCHAR(255);
ALTER TABLE job ADD COLUMN IF NOT EXISTS employment_type VARCHAR(100);
ALTER TABLE job ADD COLUMN IF NOT EXISTS openings INT;
ALTER TABLE job ADD COLUMN IF NOT EXISTS open_date DATE;
ALTER TABLE job ADD COLUMN IF NOT EXISTS close_date DATE;
ALTER TABLE job ADD COLUMN IF NOT EXISTS mode VARCHAR(100);
ALTER TABLE job ADD COLUMN IF NOT EXISTS requirements TEXT;
ALTER TABLE job ADD COLUMN IF NOT EXISTS perks TEXT;
ALTER TABLE job ADD COLUMN IF NOT EXISTS responsibilities TEXT;
ALTER TABLE job ADD COLUMN IF NOT EXISTS emp_id BIGINT;

-- Foreign key (use plural table name if that's your actual one)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_job_employee'
    ) THEN
        ALTER TABLE job
        ADD CONSTRAINT fk_job_employee
        FOREIGN KEY (emp_id) REFERENCES employees(id)
        ON DELETE SET NULL;
    END IF;
END $$;


-- ==========================
-- 🔗 USER ↔ JOB JOIN TABLE
-- ==========================
CREATE TABLE IF NOT EXISTS user_applied_jobs (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    job_id BIGINT REFERENCES job(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, job_id)
);

-- ============================================================
-- ✅ SCHEMA SYNCHRONIZED SUCCESSFULLY
-- ============================================================
