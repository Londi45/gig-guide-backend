-- V16: Rename has_v_i_p_area to hasviparea to match the @Column(name = "hasviparea") in Clubs.java
ALTER TABLE clubs RENAME COLUMN has_v_i_p_area TO hasviparea;
