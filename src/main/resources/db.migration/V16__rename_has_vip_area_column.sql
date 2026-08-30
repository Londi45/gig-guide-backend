-- V16: Rename has_v_i_p_area to hasviparea to match Hibernate's column naming
-- Hibernate maps the field 'hasVIPArea' to 'hasviparea' (all lowercase, no separators)
-- The original V4 migration used 'has_v_i_p_area' which Hibernate cannot find.

ALTER TABLE clubs RENAME COLUMN has_v_i_p_area TO hasviparea;
