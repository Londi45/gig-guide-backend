-- V13: Create my_app_user table
-- Legacy entity (MyAppUser model) — kept for backward compatibility
-- This is the original user model used before the full User entity was introduced

CREATE TABLE IF NOT EXISTS my_app_user (
    id                  BIGSERIAL    PRIMARY KEY,
    username            VARCHAR(255),
    email               VARCHAR(255),
    password            VARCHAR(255),
    verfication_token   VARCHAR(255),
    is_verified         BOOLEAN      NOT NULL DEFAULT FALSE,
    reset_token         VARCHAR(255)
);

CREATE INDEX idx_my_app_user_email ON my_app_user (email);
