-- Migración Flyway V7: Tabla de Auditoría e Historial de Asignaciones de Comuna a Inspectores

CREATE TABLE IF NOT EXISTS historial_asignaciones_comuna (
    id BIGSERIAL PRIMARY KEY,
    inspector_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    inspector_nombre VARCHAR(100) NOT NULL,
    inspector_email VARCHAR(100) NOT NULL,
    comuna_id BIGINT REFERENCES comunas(id) ON DELETE SET NULL,
    comuna_nombre VARCHAR(100) NOT NULL,
    accion VARCHAR(50) NOT NULL, -- 'ASIGNACION', 'DESASIGNACION', 'DESACTIVACION_USUARIO'
    motivo VARCHAR(255),
    ejecutado_por_email VARCHAR(100),
    fecha_hora TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_historial_asignaciones_inspector_id 
    ON historial_asignaciones_comuna(inspector_id);

CREATE INDEX IF NOT EXISTS idx_historial_asignaciones_comuna_id 
    ON historial_asignaciones_comuna(comuna_id);

CREATE INDEX IF NOT EXISTS idx_historial_asignaciones_fecha_hora 
    ON historial_asignaciones_comuna(fecha_hora);
