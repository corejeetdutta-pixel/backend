-- Ensure emp_id exists, is unique, and not null
ALTER TABLE employees ADD COLUMN IF NOT EXISTS emp_id VARCHAR(255);
ALTER TABLE employees ALTER COLUMN emp_id SET NOT NULL;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'employees_emp_id_key') THEN
        ALTER TABLE employees ADD CONSTRAINT employees_emp_id_key UNIQUE (emp_id);
    END IF;
END $$;

-- Ensure verified exists and has correct default
ALTER TABLE employees ADD COLUMN IF NOT EXISTS verified BOOLEAN DEFAULT FALSE;
ALTER TABLE employees ALTER COLUMN verified SET NOT NULL;

-- Ensure name constraints
ALTER TABLE employees ALTER COLUMN name TYPE VARCHAR(50);
ALTER TABLE employees ALTER COLUMN name SET NOT NULL;

-- Ensure email is unique and not null
ALTER TABLE employees ALTER COLUMN email SET NOT NULL;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'employees_email_key') THEN
        ALTER TABLE employees ADD CONSTRAINT employees_email_key UNIQUE (email);
    END IF;
END $$;

-- Ensure password exists, required, and length
ALTER TABLE employees ALTER COLUMN password TYPE VARCHAR(255);
ALTER TABLE employees ALTER COLUMN password SET NOT NULL;

-- Ensure mobile exists and is required
ALTER TABLE employees ADD COLUMN IF NOT EXISTS mobile VARCHAR(255);
ALTER TABLE employees ALTER COLUMN mobile SET NOT NULL;

-- Ensure address exists and supports text
ALTER TABLE employees ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE employees ALTER COLUMN address SET NOT NULL;

-- Ensure gender exists
ALTER TABLE employees ADD COLUMN IF NOT EXISTS gender VARCHAR(255);
ALTER TABLE employees ALTER COLUMN gender SET NOT NULL;

-- Ensure date_of_birth exists
ALTER TABLE employees ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE employees ALTER COLUMN date_of_birth SET NOT NULL;

-- Ensure aadhar_number exists, required, and unique
--ALTER TABLE employees ADD COLUMN IF NOT EXISTS aadhar_number VARCHAR(255);
--ALTER TABLE employees ALTER COLUMN aadhar_number SET NOT NULL;
--DO $$ BEGIN
--    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'employees_aadhar_number_key') THEN
--        ALTER TABLE employees ADD CONSTRAINT employees_aadhar_number_key UNIQUE (aadhar_number);
--    END IF;
--END $$;

-- Ensure pan_number exists, required, and unique
--ALTER TABLE employees ADD COLUMN IF NOT EXISTS pan_number VARCHAR(255);
--ALTER TABLE employees ALTER COLUMN pan_number SET NOT NULL;
--DO $$ BEGIN
--    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'employees_pan_number_key') THEN
--        ALTER TABLE employees ADD CONSTRAINT employees_pan_number_key UNIQUE (pan_number);
--    END IF;
--END $$;

-- Ensure profile_picture exists
ALTER TABLE employees ADD COLUMN IF NOT EXISTS profile_picture TEXT;

-- Ensure role exists and default is EMPLOYER
ALTER TABLE employees ADD COLUMN IF NOT EXISTS role VARCHAR(255) DEFAULT 'EMPLOYER';
ALTER TABLE employees ALTER COLUMN role SET NOT NULL;

-- Ensure agreed_to_terms exists and not null
ALTER TABLE employees ADD COLUMN IF NOT EXISTS agreed_to_terms BOOLEAN DEFAULT FALSE;
ALTER TABLE employees ALTER COLUMN agreed_to_terms SET NOT NULL;

-- Ensure created_at and updated_at exist
ALTER TABLE employees ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE employees ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;