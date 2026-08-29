-- V11: Create staff_assignments table
-- Depends on: events (V8), users (V5)
-- Extends BaseEntity (id, created_at, updated_at, active)

CREATE TABLE IF NOT EXISTS staff_assignments (
    id         BIGSERIAL PRIMARY KEY,
    event_id   BIGINT    NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    user_id    BIGINT    NOT NULL REFERENCES users  (id) ON DELETE CASCADE,
    active     BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    -- Prevent duplicate assignments for the same staff member on the same event
    CONSTRAINT uq_staff_assignments_event_user UNIQUE (event_id, user_id)
);

-- Primary lookups: get all staff for an event, get all events for a staff member
CREATE INDEX idx_staff_assignments_event_id ON staff_assignments (event_id);
CREATE INDEX idx_staff_assignments_user_id  ON staff_assignments (user_id);
