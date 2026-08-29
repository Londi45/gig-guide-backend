-- V10: Create discounts table
-- Depends on: events (V8)
-- Extends BaseEntity (id, created_at, updated_at, active)

CREATE TABLE IF NOT EXISTS discounts (
    id             BIGSERIAL      PRIMARY KEY,
    discount_type  VARCHAR(100)   NOT NULL,
    discount_value NUMERIC(10, 2) NOT NULL,
    description    TEXT,
    valid_from     TIMESTAMP,
    valid_until    TIMESTAMP,
    event_id       BIGINT         NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    active         BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,

    CONSTRAINT chk_discounts_value        CHECK (discount_value >= 0),
    CONSTRAINT chk_discounts_valid_range  CHECK (valid_until IS NULL OR valid_from IS NULL OR valid_until > valid_from)
);

-- All discounts are fetched together with their parent event
CREATE INDEX idx_discounts_event_id   ON discounts (event_id);

-- Filter active discounts by validity window
CREATE INDEX idx_discounts_valid_from  ON discounts (valid_from)  WHERE valid_from IS NOT NULL;
CREATE INDEX idx_discounts_valid_until ON discounts (valid_until) WHERE valid_until IS NOT NULL;
