-- Rename the user table to users for PostgreSQL reserved keyword fix
ALTER TABLE IF EXISTS "user" RENAME TO users;
