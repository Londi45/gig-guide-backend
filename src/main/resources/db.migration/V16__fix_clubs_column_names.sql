-- V16: Fix column name mismatch between Flyway migration and Hibernate entity
--
-- Hibernate maps 'hasVIPArea' -> 'hasviparea' (lowercase, no underscores)
-- but V4 created the column as 'has_v_i_p_area'.
-- Rename to match what Hibernate expects.

ALTER TABLE clubs RENAME COLUMN has_v_i_p_area TO hasviparea;
