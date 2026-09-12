-- Migración V6: Agregar flag explícito para Administrador General con restricción de unicidad parcial

-- 1. Agregar columna booleana es_administrador_general con valor por defecto false
ALTER TABLE usuarios 
    ADD COLUMN IF NOT EXISTS es_administrador_general BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Marcar al Administrador General actual (por ID 1 o email de origen)
UPDATE usuarios 
SET es_administrador_general = TRUE 
WHERE id = 1 OR email = 'admin@reciclajelitoral.cl';

-- 3. Crear índice único parcial para asegurar que solo exista a lo sumo un Administrador General
CREATE UNIQUE INDEX IF NOT EXISTS idx_unico_administrador_general 
    ON usuarios (es_administrador_general) 
    WHERE es_administrador_general = TRUE;
