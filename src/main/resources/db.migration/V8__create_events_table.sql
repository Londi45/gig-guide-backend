-- V8: Create events table

CREATE TABLE IF NOT EXISTS events (
    id                BIGSERIAL    PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    description       TEXT,
    start_date_time   TIMESTAMP    NOT NULL,
    end_date_time     TIMESTAMP    NOT NULL,
    genre             VARCHAR(100),
    dress_code        VARCHAR(255),
    age_restriction   VARCHAR(50),
    image_url         VARCHAR(500),
    status            VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                          CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED')),
    capacity          INT          NOT NULL DEFAULT 0,
    male_ratio        INT          NOT NULL DEFAULT 50,
    female_ratio      INT          NOT NULL DEFAULT 50,
    live_male_count   INT          NOT NULL DEFAULT 0,
    live_female_count INT          NOT NULL DEFAULT 0,
    club_id           BIGINT       NOT NULL REFERENCES clubs (id) ON DELETE CASCADE,
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,

    CONSTRAINT chk_events_end_after_start   CHECK (end_date_time > start_date_time),
    CONSTRAINT chk_events_capacity_positive CHECK (capacity >= 0),
    CONSTRAINT chk_events_ratios_sum        CHECK (male_ratio + female_ratio = 100),
    CONSTRAINT chk_events_live_counts       CHECK (live_male_count >= 0 AND live_female_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_events_status_start   ON events (status, start_date_time);
CREATE INDEX IF NOT EXISTS idx_events_club_id        ON events (club_id);
CREATE INDEX IF NOT EXISTS idx_events_club_id_status ON events (club_id, status);
CREATE INDEX IF NOT EXISTS idx_events_club_id_start  ON events (club_id, start_date_time);
CREATE INDEX IF NOT EXISTS idx_events_active         ON events (active);
