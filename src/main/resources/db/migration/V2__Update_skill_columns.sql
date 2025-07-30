-- Update user_offers table to use skill_name instead of skill_id
ALTER TABLE user_offers DROP COLUMN IF EXISTS skill_id;
ALTER TABLE user_offers ADD COLUMN skill_name VARCHAR(255) NOT NULL DEFAULT '';

-- Update user_wants table to use skill_name instead of skill_id  
ALTER TABLE user_wants DROP COLUMN IF EXISTS skill_id;
ALTER TABLE user_wants ADD COLUMN skill_name VARCHAR(255) NOT NULL DEFAULT '';

-- Update indexes
DROP INDEX IF EXISTS idx_user_offers_skill_id;
DROP INDEX IF EXISTS idx_user_wants_skill_id;
CREATE INDEX idx_user_offers_skill_name ON user_offers(skill_name);
CREATE INDEX idx_user_wants_skill_name ON user_wants(skill_name); 