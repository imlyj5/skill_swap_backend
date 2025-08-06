-- Add skill_id foreign key columns to user_offers and user_wants tables

-- Add skill_id column to user_offers
ALTER TABLE user_offers 
ADD COLUMN skill_id BIGINT;

-- Add skill_id column to user_wants  
ALTER TABLE user_wants 
ADD COLUMN skill_id BIGINT;

-- Add foreign key constraints
ALTER TABLE user_offers 
ADD CONSTRAINT fk_user_offers_skill 
FOREIGN KEY (skill_id) REFERENCES skill(id);

ALTER TABLE user_wants 
ADD CONSTRAINT fk_user_wants_skill 
FOREIGN KEY (skill_id) REFERENCES skill(id);

-- Update existing records to link to skills based on skillName
-- This will set skill_id based on matching skill names
UPDATE user_offers 
SET skill_id = skill.id 
FROM skill 
WHERE user_offers.skill_name = skill.name;

UPDATE user_wants 
SET skill_id = skill.id 
FROM skill 
WHERE user_wants.skill_name = skill.name;