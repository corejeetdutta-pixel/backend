-- Remove orphaned user_skills rows that reference non-existent users
DELETE FROM user_skills
WHERE user_id NOT IN (SELECT id FROM users);
