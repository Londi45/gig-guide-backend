-- V14: Create failed_kafka_events table
-- Stores Kafka publish failures for retry and auditing.
-- A record is written here when the producer's async callback receives an error.

CREATE TABLE IF NOT EXISTS failed_kafka_events (
    id              BIGSERIAL       PRIMARY KEY,
    topic           VARCHAR(255)    NOT NULL,
    message_key     VARCHAR(255),
    payload         TEXT            NOT NULL,
    error_message   TEXT            NOT NULL,
    retry_count     INT             NOT NULL DEFAULT 0,
    resolved        BOOLEAN         NOT NULL DEFAULT FALSE,
    failed_at       TIMESTAMP       NOT NULL,
    last_retried_at TIMESTAMP
);

-- Primary query: find all unresolved failures for retry processing
CREATE INDEX IF NOT EXISTS idx_failed_kafka_events_resolved   ON failed_kafka_events (resolved) WHERE resolved = FALSE;

-- Filter failures by topic (useful when retrying a specific event type)
CREATE INDEX IF NOT EXISTS idx_failed_kafka_events_topic      ON failed_kafka_events (topic);

-- Sort and filter by time
CREATE INDEX IF NOT EXISTS idx_failed_kafka_events_failed_at  ON failed_kafka_events (failed_at);
