-- V3__add_san_antonio_comuna_and_containers.sql: Carga de Comuna San Antonio y sus 55 contenedores iniciales

-- 1. Registrar Comuna de San Antonio
INSERT INTO comunas (nombre, codigo_region) VALUES ('San Antonio', 'V') ON CONFLICT (nombre) DO NOTHING;

-- 2. Contenedores de San Antonio (55 en total: 22 Llolleo, 27 San Antonio, 6 Leyda)
-- ==============================================================================
-- SECTOR: LLOLLEO (22 Contenedores)
-- ==============================================================================
INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'CONDOMINIO PUERTAS DE SANTO DOMINGO', 'Camino Leyda a San Juan km 6, 120 - G-904, San Juan', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/52JP3THAF4opvbh98', -33.6426659, -71.5053711
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'CONDOMINIO LAS COLINAS DE SANTO DOMINGO', 'Camino Leyda a San Juan - G-904 km 5, San Juan', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/bmpM16nH2PedR2ys6', -33.6329117, -71.5069351
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'PLAZA  LLOLLEO', 'PROVIDENCIA CON FRANCIA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.61296844482422%2C-71.6109390258789&z=17&hl=es', -33.61296844482422, -71.6109390258789
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'ESQUINA', 'PROVIDENCIA CON MEXICO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.61157989501953%2C-71.61729431152344&z=17&hl=es', -33.61157989501953, -71.61729431152344
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'MEXICO TEJAS VERDES', 'CLUB DEPORTIVO HURACAN', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.610877990722656%2C-71.61885070800781&z=17&hl=es', -33.610877990722656, -71.61885070800781
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'PLAZA LOS PESCADORES', 'LUIS MARTINEZ CRUZ CON DEL ESTERO TEJAS VERDES', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.617191314697266%2C-71.62281036376953&z=17&hl=es', -33.617191314697266, -71.62281036376953
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'PLAZA LOS PESCADORES', 'LUIS MARTINEZ CRUZ CON DEL ESTERO TEJAS VERDES (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.617191314697266%2C-71.62281036376953&z=17&hl=es', -33.617191314697266, -71.62281036376953
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'CALETA PESCADORES', 'BOCA RIO MAIPO c / L CABRERA TEJAS VERDES', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.61913299560547%2C-71.6223373413086&z=17&hl=es', -33.61913299560547, -71.6223373413086
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'PUNTO LIMPIO CODELCO', 'REGIMIENTO TEJAS VERDES', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.61910629272461%2C-71.61893463134766&z=17&hl=es', -33.61910629272461, -71.61893463134766
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'ROTONDA', 'PLAZA LA ESTRELLA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.61369323730469%2C-71.61528778076172&z=17&hl=es', -33.61369323730469, -71.61528778076172
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'ROTONDA', 'PLAZA LA ESTRELLA (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.61369323730469%2C-71.61528778076172&z=17&hl=es', -33.61369323730469, -71.61528778076172
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'PARADERO (CAMPANA MUNICIPAL)', 'CAMINO SAN JUAN 2710', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.63018798828125%2C-71.59822082519531&z=17&hl=es', -33.63018798828125, -71.59822082519531
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'ESTADIO LO GALLARDO AV. COSTANERA DEL MAIPO', 'A UN COSTADO DEL ESTERO SAN JUAN', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.63147735595703%2C-71.59854888916016&z=17&hl=es', -33.63147735595703, -71.59854888916016
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'ALFONSO FARIAS', 'ALFONSO FARIAS CON BAQUEDANO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.61798095703125%2C-71.606201171875&z=17&hl=es', -33.61798095703125, -71.606201171875
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'PASAJE 4 CON OLEGARIO HENRIQUEZ C/N LOS ALERCES', 'PASAJE 4 CON OLEGARIO HENRIQUEZ C/N LOS ALERCES', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.620182037353516%2C-71.59982299804688&z=17&hl=es', -33.620182037353516, -71.59982299804688
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'PUNTO CENTRO VOLUMINOSO', 'CALLE GINEBRA c/ OLEGARIO HENRIQUEZ', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.61178207397461%2C-71.60152435302734&z=17&hl=es', -33.61178207397461, -71.60152435302734
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'GINEBRA', 'Domingo Garcia Huidobro c/Parinacota Llolleo', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.6126815%2C-71.5969012&z=17&hl=es', -33.6126815, -71.5969012
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'PUNTO LIMPIO', 'OLEGARIIO HENRIQUEZ CN/ LOS CONDORES', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.61407470703125%2C-71.60145568847656&z=17&hl=es', -33.61407470703125, -71.60145568847656
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'ESQUINA', 'AROMOS CON CONSTITUCION', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.60817337036133%2C-71.5987548828125&z=17&hl=es', -33.60817337036133, -71.5987548828125
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'VICUÑA MACKENNA 087 ESQUINA AV. DIVINA PROVIDENCIA', 'ESTACIÓN DE TRENES', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/cA3NJm3yySq9TZuJ7', -33.6103333, -71.6121389
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'ESTADIO CALLE LAS ACACIAS', 'LAS ACACIAS LLOLLEO', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LLOLLEO', 'PARQUE   DYR', 'ACOPIO MUNICIPAL LLO LLEO', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

-- ==============================================================================
-- SECTOR: SAN ANTONIO (27 Contenedores)
-- ==============================================================================
INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'CONDOMINIO O VILLA ESTORIL', 'BARROS LUCO CON 12 SUR', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.60740661621094%2C-71.61215209960938&z=17&hl=es', -33.60740661621094, -71.61215209960938
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'LOS CEREZOS (CAMPANA MUNICIPAL)', 'BARROS LUCO CON 12 SUR', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.60785675048828%2C-71.61515045166016&z=17&hl=es', -33.60785675048828, -71.61515045166016
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'PUERTO SAN ANTONIO', 'FRENTE RESTAURANT EL DORADO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.580909729003906%2C-71.61481475830078&z=17&hl=es', -33.580909729003906, -71.61481475830078
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'PUERTO SAN ANTONIO', 'FRENTE RESTAURANT EL DORADO (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.580909729003906%2C-71.61481475830078&z=17&hl=es', -33.580909729003906, -71.61481475830078
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'PUNTO LIMPIO MUNI SAN ANTONIO (CAMPANA MUNICIPAL)', 'BARROS LUCO CON EL MOLO BARRANCAS', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.595882415771484%2C-71.61347198486328&z=17&hl=es', -33.595882415771484, -71.61347198486328
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'PUNTO LIMPIO MUNI SAN ANTONIO', 'BARROS LUCO CON EL MOLO BARRANCAS', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.595882415771484%2C-71.61347198486328&z=17&hl=es', -33.595882415771484, -71.61347198486328
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'CALLE 10 SUR / BACCIARINI', 'SECTOR LAS DUNAS', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.60532760620117%2C-71.61021423339844&z=17&hl=es', -33.60532760620117, -71.61021423339844
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'ROBERTO PARRA CON AKIN SOTO', 'PLAZA DE SAN ANTONIO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.593658447265625%2C-71.593505859375&z=17&hl=es', -33.593658447265625, -71.593505859375
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'AKIN SOTO C/N PASAJE RELONCAVI', 'AKIN SOTO C/N PASAJE RELONCAVI', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.59251403808594%2C-71.59805297851562&z=17&hl=es', -33.59251403808594, -71.59805297851562
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'PUNTO LIMPIO (CAMPANA MUNICIPAL)', 'MAESTRANZA C/N SAMUEL GARCIA HUIDOBRO', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.5907096862793%2C-71.60720825195312&z=17&hl=es', -33.5907096862793, -71.60720825195312
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'EDIFICIOS CERRO ARENA', 'LAS QUILAS CON FERNANDEZ CONCHA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.58373260498047%2C-71.60758209228516&z=17&hl=es', -33.58373260498047, -71.60758209228516
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'PUNTO LIMPIO UNION VECINAL Nº 12', 'LUIS REUSS CON ANGEL ORTUZAR BARRANCAS', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.587493896484375%2C-71.60054779052734&z=17&hl=es', -33.587493896484375, -71.60054779052734
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'PUNTO LIMPIO', 'PORTALES CON LAUTARO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.579010009765625%2C-71.60356140136719&z=17&hl=es', -33.579010009765625, -71.60356140136719
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'SARGENTO ALDEA BELLAVISTA', 'CALLE IGNACIO CERDA X SARGENTO ALDEA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.576263427734375%2C-71.6099853515625&z=17&hl=es', -33.576263427734375, -71.6099853515625
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'BANDEJON', 'CHORRILLOS CON CENTENARIO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.58243179321289%2C-71.60069274902344&z=17&hl=es', -33.58243179321289, -71.60069274902344
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'GABRIELA MISTRAL CON LAS BODEGAS', 'GABRIELA MISTRAL CON LAS BODEGAS', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.57284927368164%2C-71.60456085205078&z=17&hl=es', -33.57284927368164, -71.60456085205078
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'PUNTO LIMPIO PLAZA', 'NUEVA BRUSELAS c / GLORIA ARTIGA consultorio', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.567745208740234%2C-71.60599517822266&z=17&hl=es', -33.567745208740234, -71.60599517822266
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'PUNTO LIMPIO', 'E RIQUELME CON ARTURO PRAT', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.576210021972656%2C-71.61786651611328&z=17&hl=es', -33.576210021972656, -71.61786651611328
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'PUNTO LIMPIO ESQUINA CERRO ALEGRE', 'INFANTE ISABEL CON CALLE ASTURIAS', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.579185485839844%2C-71.61810302734375&z=17&hl=es', -33.579185485839844, -71.61810302734375
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'PUNTO LIMPIO Alfonso XIII', 'INFANTE ISABEL CON CALLE ASTURIAS', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.579559326171875%2C-71.62008666992188&z=17&hl=es', -33.579559326171875, -71.62008666992188
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'BORDE COSTERO', 'ENTRE SAN ANTONIO Y CARTAGENA Frente Centro recreacional PDI Pelancura', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.5669368%2C-71.6219397&z=17&hl=es', -33.5669368, -71.6219397
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'AGUA BUENA', 'SALIDA SAN ANTONIO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.563594818115234%2C-71.55889129638672&z=17&hl=es', -33.563594818115234, -71.55889129638672
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'MALVILLA', 'CAMINO A STGO PASO NIVEL CASABLANCA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.5873984%2C-71.5286039&z=17&hl=es', -33.5873984, -71.5286039
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'SALIDA A CARRETERA ( LAS ACACIAS COCA-COLA )', 'SALIDA A SANTIAGO KMS 7 ORILLA BERMA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.566070556640625%2C-71.58466339111328&z=17&hl=es', -33.566070556640625, -71.58466339111328
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'JUNTA DE VECINOS ENTRADA SAN ANTONIO', 'SUBIDA PUENTE AREVALO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.566070556640625%2C-71.58466339111328&z=17&hl=es', -33.566070556640625, -71.58466339111328
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'CAMPAMENTO ESPERANZA', 'SALIDA SANTIAGO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.56771469116211%2C-71.5929946899414&z=17&hl=es', -33.56771469116211, -71.5929946899414
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'SAN ANTONIO', 'ENJOY CASINO', 'SECTOR CALETA (fuera acceso casino)', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

-- ==============================================================================
-- SECTOR: LEYDA (6 Contenedores)
-- ==============================================================================
INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LEYDA', 'VIÑA GARCES SILVA', 'CUNCUMEN', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LEYDA', 'CENTRO PUEBLO PLAZA LADO DE CARABINEROS', 'CUMCUMEN', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LEYDA', 'CUMCUMEN SECTOR HUINCA', 'FUERA PUEBLO CUNCUMEN CRUCE EL ASILO AFUERA DEL COLEGIO', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LEYDA', 'JUNTA DE VECINOS', 'LEYDA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.61267852783203%2C-71.4461441040039&z=17&hl=es', -33.61267852783203, -71.4461441040039
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LEYDA', 'JUNTA DE VECINOS', 'LEYDA (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.61267852783203%2C-71.4461441040039&z=17&hl=es', -33.61267852783203, -71.4461441040039
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'LEYDA', 'VIÑA CASA MARIN', 'LO ABARCA', 'EMPRESA', 500.0, '', NULL, NULL
FROM comunas WHERE nombre = 'San Antonio' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;
