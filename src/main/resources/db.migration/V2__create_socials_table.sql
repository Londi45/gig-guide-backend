-- V2: Create socials table

CREATE TABLE IF NOT EXISTS socials (
    id              BIGSERIAL    PRIMARY KEY,
    facebook_link   VARCHAR(500),
    instagram_link  VARCHAR(500),
    twitter_link    VARCHAR(500),
    tiktok_link     VARCHAR(500)
);
