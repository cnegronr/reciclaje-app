-- V4__add_cartagena_comuna_and_containers.sql: Carga de Comuna Cartagena y sus 23 contenedores iniciales

-- 1. Registrar Comuna de Cartagena
INSERT INTO comunas (nombre, codigo_region) VALUES ('Cartagena', 'V') ON CONFLICT (nombre) DO NOTHING;

-- 2. Contenedores de Cartagena (23 en total: 14 Cartagena, 4 Lo Abarca, 5 Costanera Cartagena)
-- ==============================================================================
-- SECTOR: CARTAGENA (14 Contenedores)
-- ==============================================================================
INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'PLAYA GRANDE', 'AFUERA GIMNASIO', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'ACOPIO MUNICIPAL (CAMPANA MUNICIPAL)', 'CEAM (Centro Educativo Ambiental Municipal, EX  PLAZA DE ARMAS)', 'MUNICIPAL', 1000.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'ACOPIO MUNICIPAL (CAMPANA MUNICIPAL)', 'CEAM (Centro Educativo Ambiental Municipal, EX  PLAZA DE ARMAS) (Punto 2)', 'MUNICIPAL', 1000.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'ACOPIO MUNICIPAL (CAMPANA MUNICIPAL)', 'CEAM (Centro Educativo Ambiental Municipal, EX  PLAZA DE ARMAS) (Punto 3)', 'MUNICIPAL', 1000.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'ACOPIO MUNICIPAL - SACAS (CAMPANA MUNICIPAL)', 'CEAM (Centro Educativo Ambiental Municipal, EX  PLAZA DE ARMAS)', 'MUNICIPAL', 1000.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'CALLE LOS ALMENDROS CON ECHAURREN', 'VISTA HERMOSA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.55374526977539%2C-71.61974334716797&z=17&hl=es', -33.55374526977539, -71.61974334716797
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'ACCESO VILLA CARTAGO', 'JJ PRIETO ACCESO X GENERAL BURGOS SUBIDA POR BULNES', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'CAUPOLICAN  1Y 2', 'PSJE MIRAFLORES ENTRE LAUTARO Y FRESIA CENTRO M.', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'PLAZA / PUDETO (CAMPANA MUNICIPAL)', 'HERMANOS TOBAR CON SANTIAGO', 'MUNICIPAL', 1000.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'PUNTO LIMPIO CENTRO CIVICO', 'CAUPOLICAN 3/ LAS ACACIAS FRENTE JARDIN INFANTIL', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'El Progreso Jaula PET 1', 'Crescente errazuriz con Calle Talca Colchagua', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'El Progreso Jaula PET 2', 'Crescente errazuriz con bombero arriola', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'SEDE PUERTO NUEVO', 'CAPITAN JUAN DE CARTAGENA CON LA CALETA', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'CARTAGENA', 'VILLA LOS POETAS', 'CALLE RESURRECCIÓN CON PSJE LUIS ENRIQUE DELANO', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

-- ==============================================================================
-- SECTOR: LO ABARCA (4 Contenedores)
-- ==============================================================================
INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LO ABARCA', 'PUNTO LIMPIO LO ABARCA LAS PATAGUAS (CAMPANA MUNICIPAL)', 'SEDE LO ABARCA X LA ESCUELA CALLE LA PAZ S/N', 'MUNICIPAL', 1000.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LO ABARCA', 'RESTUARANT LO ABARCA EL SAUCE (CAMPANA MUNICIPAL)', 'EL SAUCE', 'MUNICIPAL', 1000.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LO ABARCA', 'RESTUARANT LO ABARCA EL SAUCE - SACAS (CAMPANA MUNICIPAL)', 'EL SAUCE', 'MUNICIPAL', 1000.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LO ABARCA', 'LO ZARATE', 'COSTADO ESCUELA SUSTENTABLE, CALLE SIN NUMERO', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

-- ==============================================================================
-- SECTOR: COSTANERA CARTAGENA (5 Contenedores)
-- ==============================================================================
INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'COSTANERA CARTAGENA', 'SEDE VECINAL SAN SEBASTIAN', 'AV SEXTA ORIENTE PLAYA, AL LADO DE BOMBEROS', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'COSTANERA CARTAGENA', 'PLAZA COSTA AZUL', 'POR AVENIDA COSTA AZUL HACIA LA PLAYA', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'COSTANERA CARTAGENA', 'PLAZA PONCE PUNTO NUEVO', 'AV LA PLAZA CON PRIMERA PONIENTE', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'COSTANERA CARTAGENA', 'KURT KLEMM', 'KURT KLEMM CON JOSE MIHGUEL CARRERA', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'COSTANERA CARTAGENA', '5 DE SEPTIEMBRE', '5 DE SEPTIEMBRE CON AV SANTIAGO LOVELUCK', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'Cartagena' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;
