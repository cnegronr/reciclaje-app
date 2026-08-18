-- V3__fix_inspector_assignments.sql: Asignación única de inspectores por comuna y actualización de Carlos Negrón

-- 1. Actualizar el nombre del usuario inspector@reciclajelitoral.cl a Carlos Negrón
UPDATE usuarios 
SET nombre = 'Carlos Negrón' 
WHERE email = 'inspector@reciclajelitoral.cl';

-- 2. Eliminar asignaciones para usuarios que no sean del rol INSPECTOR (ej. ADMIN o CHOFER)
DELETE FROM asignaciones_inspector
WHERE inspector_id IN (
    SELECT id FROM usuarios WHERE rol IN ('ADMIN', 'CHOFER')
);

-- 3. Eliminar duplicados en asignaciones_inspector conservando el registro más antiguo por comuna
DELETE FROM asignaciones_inspector a
USING asignaciones_inspector b
WHERE a.id > b.id AND a.comuna_id = b.comuna_id;

-- 4. Garantizar que Carlos Negrón sea el inspector asignado a El Quisco y Algarrobo
INSERT INTO asignaciones_inspector (inspector_id, comuna_id)
SELECT u.id, c.id
FROM usuarios u, comunas c
WHERE u.email = 'inspector@reciclajelitoral.cl'
  AND c.nombre IN ('El Quisco', 'Algarrobo')
ON CONFLICT DO NOTHING;

-- 5. Agregar la restricción UNIQUE(comuna_id) para garantizar a nivel de esquema un único inspector por comuna
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'unique_comuna_inspector'
    ) THEN
        ALTER TABLE asignaciones_inspector ADD CONSTRAINT unique_comuna_inspector UNIQUE (comuna_id);
    END IF;
END $$;

-- 6. Consolidar inspecciones_semanales duplicadas conservando la cabecera más reciente por comuna, tipo_ruta, semana_numero y anio
DELETE FROM inspecciones_semanales a
USING inspecciones_semanales b
WHERE a.id < b.id 
  AND a.comuna_id = b.comuna_id 
  AND COALESCE(a.tipo_ruta, 'INSPECTOR') = COALESCE(b.tipo_ruta, 'INSPECTOR') 
  AND a.semana_numero = b.semana_numero 
  AND a.anio = b.anio;

