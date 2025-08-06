-- Migration to convert from multiple skills per user to single skill per user
-- This removes duplicate records and adds unique constraints

-- Step 1: Delete duplicate user_offers, keeping only the first one for each user
DELETE FROM user_offers 
WHERE id NOT IN (
    SELECT DISTINCT ON (user_id) id 
    FROM user_offers 
    ORDER BY user_id, id ASC
);

-- Step 2: Delete duplicate user_wants, keeping only the first one for each user  
DELETE FROM user_wants 
WHERE id NOT IN (
    SELECT DISTINCT ON (user_id) id 
    FROM user_wants 
    ORDER BY user_id, id ASC
);

-- Step 3: Add unique constraints to enforce one-to-one relationships
ALTER TABLE user_offers ADD CONSTRAINT uk_user_offers_user_id UNIQUE (user_id);
ALTER TABLE user_wants ADD CONSTRAINT uk_user_wants_user_id UNIQUE (user_id);