-- Add skill_id foreign key columns to user_offers and user_wants tables

-- Add skill_id column to user_offers
ALTER TABLE user_offers 
ADD COLUMN skill_id BIGINT;

-- Add skill_id column to user_wants  
ALTER TABLE user_wants 
ADD COLUMN skill_id BIGINT;

-- Create missing skills from user_offers that don't exist in skill table
INSERT INTO skill (name, category)
SELECT DISTINCT uo.skill_name, 'General'
FROM user_offers uo
WHERE uo.skill_name IS NOT NULL 
  AND uo.skill_name != ''
  AND NOT EXISTS (
    SELECT 1 FROM skill s WHERE s.name = uo.skill_name
  );

-- Create missing skills from user_wants that don't exist in skill table
INSERT INTO skill (name, category)
SELECT DISTINCT uw.skill_name, 'General'
FROM user_wants uw
WHERE uw.skill_name IS NOT NULL 
  AND uw.skill_name != ''
  AND NOT EXISTS (
    SELECT 1 FROM skill s WHERE s.name = uw.skill_name
  );

-- Update existing records to link to skills based on skillName
-- Now all skillNames should have corresponding skills
UPDATE user_offers 
SET skill_id = skill.id 
FROM skill 
WHERE user_offers.skill_name = skill.name;

UPDATE user_wants 
SET skill_id = skill.id 
FROM skill 
WHERE user_wants.skill_name = skill.name;

-- Add foreign key constraints
ALTER TABLE user_offers 
ADD CONSTRAINT fk_user_offers_skill 
FOREIGN KEY (skill_id) REFERENCES skill(id);

ALTER TABLE user_wants 
ADD CONSTRAINT fk_user_wants_skill 
FOREIGN KEY (skill_id) REFERENCES skill(id);