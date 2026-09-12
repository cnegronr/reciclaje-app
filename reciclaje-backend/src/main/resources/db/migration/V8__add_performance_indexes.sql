-- V8__add_performance_indexes.sql: Índices estratégicos para optimización de concurrencia y consultas

-- 1. Índices en claves foráneas de fotos y actualizaciones (elimina sequential scans masivos)
CREATE INDEX IF NOT EXISTS idx_fotos_detalle_inspeccion_id 
    ON fotos_inspeccion(detalle_inspeccion_id);

CREATE INDEX IF NOT EXISTS idx_fotos_actualizacion_detalle_id 
    ON fotos_inspeccion(actualizacion_detalle_id);

CREATE INDEX IF NOT EXISTS idx_actualizaciones_detalle_inspeccion_id 
    ON actualizaciones_detalle(detalle_inspeccion_id);

-- 2. Índices en detalles de inspección para búsquedas por contenedor y estado de visita
CREATE INDEX IF NOT EXISTS idx_detalles_contenedor_id 
    ON detalle_inspecciones(contenedor_id);

CREATE INDEX IF NOT EXISTS idx_detalles_visitado_fecha 
    ON detalle_inspecciones(visitado, fecha_hora_inicial) 
    WHERE visitado = true;

CREATE INDEX IF NOT EXISTS idx_detalles_creado_por 
    ON detalle_inspecciones(creado_por_usuario_id);

CREATE INDEX IF NOT EXISTS idx_detalles_actualizado_por 
    ON detalle_inspecciones(actualizado_por_usuario_id);

-- 3. Índices en asignaciones e inspecciones
CREATE INDEX IF NOT EXISTS idx_asignaciones_inspector_id 
    ON asignaciones_inspector(inspector_id);

CREATE INDEX IF NOT EXISTS idx_inspecciones_inspector_anio_semana 
    ON inspecciones_semanales(inspector_id, anio, semana_numero);

CREATE INDEX IF NOT EXISTS idx_inspecciones_comuna_anio_semana 
    ON inspecciones_semanales(comuna_id, anio DESC, semana_numero DESC);

-- 4. Índice para contenedores activos por comuna
CREATE INDEX IF NOT EXISTS idx_contenedores_comuna_activo 
    ON contenedores(comuna_id, activo);
