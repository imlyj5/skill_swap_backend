-- First, clean up any duplicate emails by keeping the oldest record
-- PostgreSQL-compatible way to delete duplicates
DELETE FROM users 
WHERE id IN (
    SELECT id FROM (
        SELECT id, 
               ROW_NUMBER() OVER (PARTITION BY email ORDER BY id) as rn
        FROM users
    ) t
    WHERE t.rn > 1
);

-- Add unique constraint and index on email column for faster login queries and prevent duplicates
ALTER TABLE users ADD CONSTRAINT unique_email UNIQUE (email);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);