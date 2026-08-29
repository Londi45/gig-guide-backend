-- V15: Change clubs.id, events.club_id, and users.club_id from BIGINT to VARCHAR(36)
--
-- Order matters:
--   1. Drop all FK constraints referencing clubs(id)
--   2. Drop identity on clubs.id
--   3. Alter ALL three columns in one step (clubs.id, events.club_id, users.club_id)
--   4. Clear any data rows (BIGINT values can't be valid UUIDs)
--   5. Re-add FK constraints now that types match

-- ── 1. Drop FK constraints that reference clubs(id) ──────────────────────────

ALTER TABLE events DROP CONSTRAINT IF EXISTS events_club_id_fkey;
ALTER TABLE users  DROP CONSTRAINT IF EXISTS users_club_id_fkey;
ALTER TABLE clubs  DROP CONSTRAINT IF EXISTS clubs_owner_user_id_fkey;

-- Also drop any Hibernate-generated FK names (fk...)
DO $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT tc.constraint_name, tc.table_name
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
      ON tc.constraint_name = kcu.constraint_name
    JOIN information_schema.referential_constraints rc
      ON tc.constraint_name = rc.constraint_name
    JOIN information_schema.key_column_usage kcu2
      ON rc.unique_constraint_name = kcu2.constraint_name
    WHERE tc.constraint_type = 'FOREIGN KEY'
      AND kcu2.table_name = 'clubs'
      AND kcu2.column_name = 'id'
  LOOP
    EXECUTE format('ALTER TABLE %I DROP CONSTRAINT IF EXISTS %I',
                   r.table_name, r.constraint_name);
  END LOOP;
END $$;

-- ── 2. Drop identity on clubs.id ─────────────────────────────────────────────

ALTER TABLE clubs ALTER COLUMN id DROP IDENTITY IF EXISTS;

-- ── 3. Clear data (BIGINT ids can't become valid UUID strings) ────────────────

DELETE FROM events;
DELETE FROM users;
DELETE FROM clubs;

-- ── 4. Alter all three columns to VARCHAR(36) ────────────────────────────────

ALTER TABLE clubs  ALTER COLUMN id       TYPE VARCHAR(36) USING NULL;
ALTER TABLE events ALTER COLUMN club_id  TYPE VARCHAR(36) USING NULL;
ALTER TABLE users  ALTER COLUMN club_id  TYPE VARCHAR(36) USING NULL;

-- ── 5. Re-add owner_user_id column (was dropped in earlier attempt, re-add safely) ──

ALTER TABLE clubs ADD COLUMN IF NOT EXISTS owner_user_id BIGINT;

-- ── 6. Re-add FK constraints now that types match ────────────────────────────

ALTER TABLE events
    ADD CONSTRAINT events_club_id_fkey
    FOREIGN KEY (club_id) REFERENCES clubs (id) ON DELETE CASCADE;

ALTER TABLE users
    ADD CONSTRAINT users_club_id_fkey
    FOREIGN KEY (club_id) REFERENCES clubs (id) ON DELETE SET NULL;

ALTER TABLE clubs
    ADD CONSTRAINT clubs_owner_user_id_fkey
    FOREIGN KEY (owner_user_id) REFERENCES users (id) ON DELETE SET NULL;

-- ── 7. Indexes ────────────────────────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS idx_clubs_owner_user_id ON clubs (owner_user_id);
