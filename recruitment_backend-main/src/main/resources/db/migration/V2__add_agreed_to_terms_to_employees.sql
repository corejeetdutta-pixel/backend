-- Migration: Add agreed_to_terms column to employees table
ALTER TABLE employees ADD COLUMN IF NOT EXISTS agreed_to_terms BOOLEAN NOT NULL DEFAULT FALSE;
