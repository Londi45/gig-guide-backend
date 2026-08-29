-- V12: Create check_in_audit_entries table
-- Depends on: events (V8)
-- Extends BaseEntity (id, created_at, updated_at, active)
-- High-write table: receives an insert on every check-in/check-out action

CREATE TABLE IF NOT EXISTS  check_in_audit_entries (
    id           BIGSERIAL   PRIMARY KEY,
    gender       VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE')),
    action       VARCHAR(10) CHECK (action IN ('CHECK_IN', 'CHECK_OUT')),
    performed_by BIGINT,     -- user id of the staff member (soft ref, no FK to avoid cascade issues)
    timestamp    TIMESTAMP   NOT NULL DEFAULT NOW(),
    event_id     BIGINT      NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP
);

-- Primary audit log query: get all entries for an event ordered by time
CREATE INDEX idx_audit_event_id_timestamp ON check_in_audit_entries (event_id, timestamp DESC);

-- Filter by action type per event (e.g. count all CHECK_INs for an event)
CREATE INDEX idx_audit_event_id_action    ON check_in_audit_entries (event_id, action);

-- Filter by staff member across events (who checked in the most?)
CREATE INDEX idx_audit_performed_by       ON check_in_audit_entries (performed_by);
