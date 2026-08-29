-- V9: Create entry_types table

CREATE TABLE IF NOT EXISTS entry_types (
    id                 BIGSERIAL       PRIMARY KEY,
    name               VARCHAR(255)    NOT NULL,
    price              NUMERIC(10, 2)  NOT NULL,
    description        TEXT,
    available_quantity INT             NOT NULL DEFAULT 0,
    event_id           BIGINT          NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    active             BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP,
    updated_at         TIMESTAMP,

    CONSTRAINT chk_entry_type_price    CHECK (price >= 0),
    CONSTRAINT chk_entry_type_quantity CHECK (available_quantity >= 0)
);

CREATE INDEX IF NOT EXISTS idx_entry_types_event_id ON entry_types (event_id);
