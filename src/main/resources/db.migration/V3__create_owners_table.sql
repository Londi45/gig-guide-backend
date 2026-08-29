-- V3: Create owners table (legacy standalone entity)

CREATE TABLE IF NOT EXISTS owners (
    id         BIGSERIAL    PRIMARY KEY,
    full_name  VARCHAR(255),
    email      VARCHAR(255),
    phone      VARCHAR(50)
);
