-- Test schema for H2 database
-- This overrides the JPA entity definitions for testing

DROP TABLE IF EXISTS skill CASCADE;
DROP TABLE IF EXISTS user_offers CASCADE;
DROP TABLE IF EXISTS user_wants CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop sequences if they exist
DROP SEQUENCE IF EXISTS skill_seq;
DROP SEQUENCE IF EXISTS user_offers_seq;
DROP SEQUENCE IF EXISTS user_wants_seq;
DROP SEQUENCE IF EXISTS users_seq;

-- Create sequences
CREATE SEQUENCE skill_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE user_offers_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE user_wants_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    availability VARCHAR(255),
    bio VARCHAR(255),
    email VARCHAR(255),
    learning_style VARCHAR(255),
    location VARCHAR(255),
    password VARCHAR(255),
    pronouns VARCHAR(255),
    username VARCHAR(255)
);

CREATE TABLE skill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(255),
    name VARCHAR(255),
    tags VARCHAR(1000) -- Store as JSON string for H2 compatibility
);

CREATE TABLE user_offers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_id BIGINT,
    skill_name VARCHAR(255),
    user_id BIGINT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_wants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_id BIGINT,
    skill_name VARCHAR(255),
    user_id BIGINT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
); 