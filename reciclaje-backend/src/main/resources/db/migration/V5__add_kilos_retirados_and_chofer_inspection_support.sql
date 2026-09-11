-- Migración V5: Soporte para inspecciones independientes de chofer y kilos retirados

-- 1. Agregar columnas para kilos retirados en detalle_inspecciones
ALTER TABLE detalle_inspecciones 
    ADD COLUMN IF NOT EXISTS kilos_retirados NUMERIC(7,2),
    ADD COLUMN IF NOT EXISTS kilos_retirados_inicial NUMERIC(7,2);

-- 2. Agregar columna para kilos retirados en actualizaciones_detalle
ALTER TABLE actualizaciones_detalle 
    ADD COLUMN IF NOT EXISTS kilos_retirados NUMERIC(7,2);

-- 3. Crear índice para optimizar consultas de inspecciones independientes por comuna, tipo de ruta, semana y año
CREATE INDEX IF NOT EXISTS idx_inspecciones_comuna_tipo_semana_anio 
    ON inspecciones_semanales(comuna_id, tipo_ruta, semana_numero, anio);

-- 4. Inicializar kilos_retirados a partir de kilos_calculados para registros existentes con ruta CHOFER
UPDATE detalle_inspecciones di
SET kilos_retirados = di.kilos_calculados,
    kilos_retirados_inicial = di.kilos_calculados_inicial
FROM inspecciones_semanales i
WHERE di.inspeccion_semanal_id = i.id AND i.tipo_ruta = 'CHOFER';
