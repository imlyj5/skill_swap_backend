-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255),
    pronouns VARCHAR(255),
    location VARCHAR(255),
    bio TEXT,
    email VARCHAR(255) NOT NULL UNIQUE,
    learning_style VARCHAR(255),
    availability VARCHAR(255),
    password VARCHAR(255) NOT NULL
);

-- Create skills table
CREATE TABLE skill (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255)
);

-- Create user_offers table (skills that users offer)
CREATE TABLE user_offers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL
);

-- Create user_wants table (skills that users want to learn)
CREATE TABLE user_wants (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL
);

-- Create indexes for better performance
CREATE INDEX idx_user_offers_user_id ON user_offers(user_id);
CREATE INDEX idx_user_offers_skill_id ON user_offers(skill_id);
CREATE INDEX idx_user_wants_user_id ON user_wants(user_id);
CREATE INDEX idx_user_wants_skill_id ON user_wants(skill_id); 