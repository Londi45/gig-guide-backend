-- V16: Rename has_v_i_p_area to hasviparea to match the Hibernate entity mapping
-- The Clubs entity uses @Column(name = "hasviparea") which Hibernate resolves to "hasviparea"
-- but V4 created the column as "has_v_i_p_area"

ALTER TABLE clubs RENAME COLUMN has_v_i_p_area TO hasviparea;
