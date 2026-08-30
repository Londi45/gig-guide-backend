-- V17: Add missing sequence for my_app_user table
-- Hibernate expects a named sequence my_app_user_seq when using @GeneratedValue

CREATE SEQUENCE IF NOT EXISTS my_app_user_seq
    START WITH 1
    INCREMENT BY 50;
