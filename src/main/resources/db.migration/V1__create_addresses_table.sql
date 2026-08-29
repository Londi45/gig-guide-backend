-- V1: Create addresses table

CREATE TABLE IF NOT EXISTS addresses (
    id           BIGSERIAL    PRIMARY KEY,
    location     VARCHAR(255),
    city         VARCHAR(100),
    province     VARCHAR(100),
    country      VARCHAR(100),
    postal_code  VARCHAR(20)
);

CREATE INDEX IF NOT EXISTS idx_addresses_city     ON addresses (city);
CREATE INDEX IF NOT EXISTS idx_addresses_province ON addresses (province);
CREATE INDEX IF NOT EXISTS idx_addresses_country  ON addresses (country);
