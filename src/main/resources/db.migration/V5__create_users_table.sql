-- V5: Create users table

CREATE TABLE IF NOT EXISTS users (
    id                          BIGSERIAL    PRIMARY KEY,
    username                    VARCHAR(100) NOT NULL UNIQUE,
    password                    VARCHAR(255) NOT NULL,
    email                       VARCHAR(255) NOT NULL UNIQUE,
    full_name                   VARCHAR(255),
    phone_number                VARCHAR(50),
    role                        VARCHAR(20)  NOT NULL
                                    CHECK (role IN ('CLUB_OWNER', 'STAFF', 'CUSTOMER', 'ADMIN')),
    club_id                     BIGINT       REFERENCES clubs (id) ON DELETE SET NULL,
    is_active                   BOOLEAN      NOT NULL DEFAULT TRUE,
    is_verified                 BOOLEAN      NOT NULL DEFAULT FALSE,
    verification_token          VARCHAR(255),
    verification_token_expiry   TIMESTAMP,
    password_reset_token        VARCHAR(255),
    password_reset_token_expiry TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email                ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_username             ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_club_id              ON users (club_id);
CREATE INDEX IF NOT EXISTS idx_users_club_id_role         ON users (club_id, role);
CREATE INDEX IF NOT EXISTS idx_users_verification_token   ON users (verification_token)   WHERE verification_token IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_password_reset_token ON users (password_reset_token) WHERE password_reset_token IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_is_active            ON users (is_active);
