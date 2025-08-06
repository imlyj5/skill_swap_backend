-- Add AI-generated tags to skill table as JSONB
-- Much simpler than separate tables and relationships

-- Add tags column to store AI-generated categories as JSONB array
ALTER TABLE skill 
ADD COLUMN tags JSONB;

-- Add index for tag queries (PostgreSQL specific with proper operator class)
CREATE INDEX idx_skill_tags ON skill USING GIN (tags jsonb_path_ops);

-- Update existing skills with some default tags for testing
UPDATE skill SET tags = '["general"]' WHERE tags IS NULL;