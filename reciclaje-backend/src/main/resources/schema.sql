-- Esquema DDL SQL de Inicialización para Reciclaje Litoral

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) DEFAULT 'INSPECTOR',
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS comunas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    codigo_region VARCHAR(10) DEFAULT 'V'
);

CREATE TABLE IF NOT EXISTS contenedores (
    id BIGSERIAL PRIMARY KEY,
    comuna_id BIGINT REFERENCES comunas(id) ON DELETE CASCADE,
    nombre_punto VARCHAR(150) NOT NULL,
    ubicacion_descripcion TEXT,
    categoria VARCHAR(20) CHECK (categoria IN ('EMPRESA', 'MUNICIPAL')),
    kilos_maximos NUMERIC(6,2) NOT NULL,
    url_google_maps TEXT NOT NULL,
    latitud NUMERIC(10,7),
    longitud NUMERIC(10,7),
    CONSTRAINT unique_contenedor_comuna_punto UNIQUE (comuna_id, nombre_punto, ubicacion_descripcion)
);

CREATE TABLE IF NOT EXISTS asignaciones_inspector (
    id BIGSERIAL PRIMARY KEY,
    inspector_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE,
    comuna_id BIGINT REFERENCES comunas(id) ON DELETE CASCADE,
    UNIQUE(inspector_id, comuna_id)
);

CREATE TABLE IF NOT EXISTS inspecciones_semanales (
    id BIGSERIAL PRIMARY KEY,
    comuna_id BIGINT REFERENCES comunas(id),
    inspector_id BIGINT REFERENCES usuarios(id),
    inspector_asociado_id BIGINT REFERENCES usuarios(id),
    tipo_ruta VARCHAR(20) DEFAULT 'INSPECTOR',
    semana_numero INT NOT NULL,
    anio INT NOT NULL,
    fecha_limite TIMESTAMP NOT NULL,
    estado VARCHAR(20) DEFAULT 'EN_PROGRESO',
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS detalle_inspecciones (
    id BIGSERIAL PRIMARY KEY,
    inspeccion_semanal_id BIGINT REFERENCES inspecciones_semanales(id) ON DELETE CASCADE,
    contenedor_id BIGINT REFERENCES contenedores(id),
    creado_por_usuario_id BIGINT REFERENCES usuarios(id),
    actualizado_por_usuario_id BIGINT REFERENCES usuarios(id),
    porcentaje_estimado NUMERIC(5,2) CHECK (porcentaje_estimado BETWEEN 0 AND 100),
    kilos_calculados NUMERIC(7,2) NOT NULL,
    porcentaje_estimado_inicial NUMERIC(5,2) CHECK (porcentaje_estimado_inicial BETWEEN 0 AND 100),
    kilos_calculados_inicial NUMERIC(7,2),
    visitado BOOLEAN DEFAULT FALSE,
    fecha_hora_inicial TIMESTAMP,
    fecha_hora_actualizacion TIMESTAMP,
    observaciones TEXT,
    observaciones_inicial TEXT,
    UNIQUE(inspeccion_semanal_id, contenedor_id)
);

CREATE TABLE IF NOT EXISTS actualizaciones_detalle (
    id BIGSERIAL PRIMARY KEY,
    detalle_inspeccion_id BIGINT REFERENCES detalle_inspecciones(id) ON DELETE CASCADE,
    usuario_id BIGINT REFERENCES usuarios(id),
    porcentaje_estimado NUMERIC(5,2) CHECK (porcentaje_estimado BETWEEN 0 AND 100),
    kilos_calculados NUMERIC(7,2) NOT NULL,
    observaciones TEXT,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fotos_inspeccion (
    id BIGSERIAL PRIMARY KEY,
    detalle_inspeccion_id BIGINT REFERENCES detalle_inspecciones(id) ON DELETE CASCADE,
    actualizacion_detalle_id BIGINT REFERENCES actualizaciones_detalle(id) ON DELETE CASCADE,
    usuario_id BIGINT REFERENCES usuarios(id),
    momento VARCHAR(25) CHECK (momento IN ('INICIAL_ANTES', 'INICIAL_DESPUES', 'ACTUALIZACION_ANTES', 'ACTUALIZACION_DESPUES')),
    url_foto TEXT NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
