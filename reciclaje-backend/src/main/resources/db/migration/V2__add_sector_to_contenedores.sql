-- V2__add_sector_to_contenedores.sql: Agregar columna sector a la tabla contenedores

ALTER TABLE contenedores ADD COLUMN IF NOT EXISTS sector VARCHAR(100);
