-- V4: Create clubs table
-- owner_user_id FK added in V6 after users table exists

CREATE TABLE IF NOT EXISTS clubs (
    id               BIGSERIAL     PRIMARY KEY,
    name             VARCHAR(255),
    description      TEXT,
    email            VARCHAR(255),
    phone            VARCHAR(50),
    website          VARCHAR(500),
    logo_url         VARCHAR(500),
    cover_image_url  VARCHAR(500),
    opening_hours    VARCHAR(50),
    closing_hours    VARCHAR(50),
    dress_code       VARCHAR(255),
    has_parking      BOOLEAN       NOT NULL DEFAULT FALSE,
    has_v_i_p_area   BOOLEAN       NOT NULL DEFAULT FALSE,
    capacity         INT           NOT NULL DEFAULT 0,
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    address_id       BIGINT        REFERENCES addresses (id) ON DELETE SET NULL,
    social_id        BIGINT        REFERENCES socials  (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_clubs_active     ON clubs (active);
CREATE INDEX IF NOT EXISTS idx_clubs_name       ON clubs (name);
CREATE INDEX IF NOT EXISTS idx_clubs_address_id ON clubs (address_id);
