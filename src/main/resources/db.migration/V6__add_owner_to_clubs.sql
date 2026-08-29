-- V6: Add owner_user_id FK to clubs (resolves circular dependency between clubs and users)

ALTER TABLE clubs
    ADD COLUMN IF NOT EXISTS owner_user_id BIGINT REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_clubs_owner_user_id ON clubs (owner_user_id);
