-- V2__seed_initial_data.sql: Carga inicial consolidada de comunas y contenedores

-- 1. Comunas Base
INSERT INTO comunas (nombre, codigo_region) VALUES ('El Quisco', 'V') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO comunas (nombre, codigo_region) VALUES ('Algarrobo', 'V') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO comunas (nombre, codigo_region) VALUES ('Santo Domingo', 'V') ON CONFLICT (nombre) DO NOTHING;

-- 2. Contenedores / Puntos Limpios (140 en total)
INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL TOTORAL', 'COLEGIO EL TOTORAL (CAMPANA MUNICIPAL)', 'FRENTE AL COLEGIO', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4203812%2C-71.6253086&z=17&hl=es', -33.4203812, -71.6253086
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL TOTORAL', 'COLEGIO EL TOTORAL (CAMPANA MUNICIPAL)', 'FRENTE AL COLEGIO (Punto 2)', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4203812%2C-71.6253086&z=17&hl=es', -33.4203812, -71.6253086
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL TOTORAL', 'CENTINELA', 'CAMINO ANTIGUO HACIA ALGARROBO (PASADO ACOPIO EL QUISCO)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/siZ5B5weUUX4z7sg6', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL TOTORAL', 'CONDOMINIO CENTINELA (2 SACAS)', 'INTERIOR CONDOMINIO FRENTE A CAMPANA', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/efvzPRidfHPVbFN77', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL TOTORAL', 'COMUNIDAD LOS QUILOS', 'LOS QUILOS', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/qTYet3wiPXAWBXPX8', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ISLA NEGRA', 'LA PERLA (CAMPANA MUNICIPAL)', 'AV. CENTRAL CON LA PERLA', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4393117%2C-71.6779972&z=17&hl=es', -33.4393117, -71.6779972
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ISLA NEGRA', 'CARMENCITA (CAMPANA MUNICIPAL)', 'AV. CENTRAL CON CARMENCITA (MAS ARRIBA)', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4393117%2C-71.6779972&z=17&hl=es', -33.4393117, -71.6779972
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ISLA NEGRA', 'LOMA LINDA JUNTA DE VECINOS (CAMPANA MUNICIPAL)', 'LOMA LINDA CON SANTA ROSA DE LIMA', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4373254%2C-71.6797372&z=17&hl=es', -33.4373254, -71.6797372
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ISLA NEGRA', 'EL SAUCE C/FATIMA NORTE (CAMPANA MUNICIPAL)', 'EL SAUCE CON FATIMA NORTE', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.435105%2C-71.6831453&z=17&hl=es', -33.435105, -71.6831453
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ISLA NEGRA', 'ISIDORO DUBOURNEIS EX RESTAURANT EL CIELO (CAMPANA MUNICIPAL)', 'ISLA NEGRA CENTRO', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4405794%2C-71.6832087&z=17&hl=es', -33.4405794, -71.6832087
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ISLA NEGRA', 'ISIDORO DUBOURNEIS EX RESTAURANTE EL CIELO (CAMPANA MUNICIPAL)', 'ISLA NEGRA CENTRO', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4405794%2C-71.6832087&z=17&hl=es', -33.4405794, -71.6832087
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ISLA NEGRA', 'ISIDORA DUBORNAIS / LA HIGUERA (CAMPANA MUNICIPAL)', 'PLAZA LOS MOSAICOS CAMBIO CAMPANA MUNICIPAL', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4375146%2C-71.6846501&z=17&hl=es', -33.4375146, -71.6846501
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'PUNTA DE TRALCA', 'PIEDRA DEL TRUENO CON COINCO (CAMPANA MUNICIPAL)', 'PIEDRA DEL TRUENO CON COINCO', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.42630386352539%2C-71.70197296142578&z=17&hl=es', -33.42630386352539, -71.70197296142578
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'PUNTA DE TRALCA', 'PLAZA DE LA MÚSICA (CAMPANA MUNICIPAL)', 'DEL MÚSICO CON AV. PUNTA DE TRALCA', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.425818%2C-71.6965914&z=17&hl=es', -33.425818, -71.6965914
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'PUNTA DE TRALCA', 'COMUNIDAD LOS ESCRITORES', 'COMUNIDAD LOS ESCRITORES', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/pVQtZVJKC7ad9xkz9', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'PUNTA DE TRALCA', 'SEDE AGUAS CLARAS (CAMPANA MUNICIPAL)', 'AGUAS CLARAS CON LA CANTERA', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4262163%2C-71.690243&z=17&hl=es', -33.4262163, -71.690243
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'PUNTA DE TRALCA', 'SUPERMERCADO OASIS AV PRINCIPAL SEMAFORO', 'AV. ISIDORO DUBOURNAIS CON AV. PUNTA DE TRALCA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.4258094%2C-71.6918963&z=17&hl=es', -33.4258094, -71.6918963
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO SUR', 'DSCOTEQUE SPEEDY WAY FRENTE FERRETERIA TOTORAL', 'AV. ISIDORA DUBOURNAIS, FRENTE A FERRETERÍA TOTORAL', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.414294%2C-71.6962142&z=17&hl=es', -33.414294, -71.6962142
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO SUR', 'VILLA TRALCAMAHUIDA', 'VILLA TRALCAMAHUIDA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.41357421875%2C-71.6965103149414&z=17&hl=es', -33.41357421875, -71.6965103149414
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO SUR', 'LIBRA CON LA MONTAÑA (CAMPANA MUNICIPAL)', 'LIBRA CON LA MONTAÑA', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4170623%2C-71.6947394&z=17&hl=es', -33.4170623, -71.6947394
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO SUR', 'EXPLENDOR', 'LA CANTERA CON LA MONTAÑA', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/TcW7xG4pchgBMvFH6', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO SUR', 'PASAJE LA PLAZA CON TRALCAMAHUIDA', 'PASAJE LA PLAZA CON TRALCAMAHUIDA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.4133413%2C-71.688308&z=17&hl=es', -33.4133413, -71.688308
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO SUR', 'LOS COPIHUES 2', '4 ORIENTE CN AV ESPAÑA TRALCAMAHUIDA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.4137497%2C-71.6821886&z=17&hl=es', -33.4137497, -71.6821886
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'LAS PARCELAS CON CALLE NUEVA (CAMPANA MUNICIPAL)', 'LAS PARCELAS CON CALLE NUEVA', 'MUNICIPAL', 1000.0, 'https://maps.app.goo.gl/r84Y7svCaAutfYTo8', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO SUR', 'ALTAS CUMBRES CON ACONCAGUA', 'ALTAS CUMBRES CON ACONCAGUA. SUBIR POR TRALCAMAHUIDA', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/3s6BW5zqiCwaBMg5A', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO SUR', 'CERRILLOS', 'CERRILLOS (FRENTE A GIMNASIO)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/VGjXZQ8Cx9yDY5xU9', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO SUR', 'PUNTO LIMPIO TERMINAL BUSES PULLMAN (CAMPANA MUNICIPAL)', 'ISIDORO DUBOURNAIS / HUALLILEMU / CAVANCHA', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4076005%2C-71.6927607&z=17&hl=es', -33.4076005, -71.6927607
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO SUR', 'LOS OLIVOS', 'A UN COSTADO TERMINAL PULLMAN BUS', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/gb1zQRrPAhF5AKQF7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO SUR', 'DEL EJERCITO C/ SERRANO', 'DEL EJERCITO C/ SERRANO. DEL TERMINAL HACIA ABAJO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.40777587890625%2C-71.69515228271484&z=17&hl=es', -33.40777587890625, -71.69515228271484
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'SUPERMERCADO AREGON (CAMPANA MUNICIPAL)', 'ISIDORO DUBOURNAIS CON CRUZ DEL SUR', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4037582%2C-71.6933522&z=17&hl=es', -33.4037582, -71.6933522
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'DEL ALBA (CAMPANA MUNICIPAL)', 'DEL ALBA CON AV. I. DUBOURNAIS', 'MUNICIPAL', 1000.0, 'https://goo.gl/maps/tKvFVqaNz3Bo4jYs9', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'MUNICIPALIDAD (CAMPANA MUNICIPAL)', 'AV. FRANCIA CON FRANCISCO FERRER GONZALEZ', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.4000299%2C-71.6933245&z=17&hl=es', -33.4000299, -71.6933245
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'AV. ESPAÑA', 'AV. ESPAÑA', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/vhYtyydadSsjhGW3A', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'ALCALDE ROMERO CON MAYORAZGO (NITRO) (ESTADIO)', 'ALCALDE ROMERO CON MAYORAZGO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.3982245%2C-71.688555&z=17&hl=es', -33.3982245, -71.688555
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'JOSÉ NARCISO AGUIRRE CON LOS AROMOS', 'CON LOS AROMOS', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.3974906%2C-71.6875332&z=17&hl=es', -33.3974906, -71.6875332
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'JOSÉ NARCISO AGUIRRE CON REGIDOR Y MARCHANT (CAMPANA MUNICIPAL)', 'NARCISO AGUIRRE ARRIBA FUERA CANCHA DE TENIS', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.3966664%2C-71.6897073&z=17&hl=es', -33.3966664, -71.6897073
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'VILLA EL QUISCO', '|', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.39876174926758%2C-71.68199920654297&z=17&hl=es', -33.39876174926758, -71.68199920654297
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'SEDE MIRADOR C/TRANSVERSAL', 'MIRADOR CON TRANSVERSAL 1 (MULTICANCHA)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.3976998%2C-71.6813441&z=17&hl=es', -33.3976998, -71.6813441
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'ANDES CON PINOMAR (CAMPANA MUNICIPAL)', 'LOMA LINDA CON SANTA ROSA DE LIMA', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.3957137%2C-71.6773839&z=17&hl=es', -33.3957137, -71.6773839
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'LAS ACACIAS C/ LOS PAPAYOS (CAMPANA MUNICIPAL)', 'LAS ACACIAS C/ LOS PAPAYOS', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.3941678%2C-71.68556&z=17&hl=es', -33.3941678, -71.68556
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'PINOMAR CON MAYORAZGO (CAMPANA MUNICIPAL)', 'PINOMAR CON MAYORAZGO', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.3954698%2C-71.6882029&z=17&hl=es', -33.3954698, -71.6882029
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'JOSÉ NARCISO AGUIRRE CON MARTE (CAMPANA MUNICIPAL)', 'JOSÉ NARCISO AGUIRRE CON MARTE. SECTOR PUNTILLA', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.3962981%2C-71.6994751&z=17&hl=es', -33.3962981, -71.6994751
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'RESTURANT CALETA MIRAMAR (CAMPANA MUNICIPAL)', 'AV. MIRAMAR c/ CALETA DE PESCADORES', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.3951983%2C-71.6969416&z=17&hl=es', -33.3951983, -71.6969416
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'RESTURANT CALETA MIRAMAR (CAMPANA MUNICIPAL)', 'AV. MIRAMAR c/ CALETA DE PESCADORES (Punto 2)', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.3951983%2C-71.6969416&z=17&hl=es', -33.3951983, -71.6969416
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'FRANCIA CON SANTO DOMINGO', 'FRANCIA C/ SANTO DOMINGO. DEL MUNICIPIO HACIA ABAJO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.3994371%2C-71.6969167&z=17&hl=es', -33.3994371, -71.6969167
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EL QUISCO CENTRO', 'RAUL ROMERO ERAZO CENTRO PARADERO DE TAXI', 'RAUL ROMERO ERAZO CON  ALCALDESA MERCEDES GODOY', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/JvHchWRXZaoCxfSn9', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'SUBIDA LOS LOBOS CAMPANA MUNICIPAL', 'PARCELA 4 ALTURA 414', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.3905297%2C-71.6882822&z=17&hl=es', -33.3905297, -71.6882822
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'SUBIDA LOS LOBOS', 'PARCELA 4 ALTURA 414', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.3905297%2C-71.6882822&z=17&hl=es', -33.3905297, -71.6882822
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'COMUNIDAD LOS 4 ASES', 'SUBIDA LOS LOBOS PARCELA 16', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.3908877%2C-71.6823135&z=17&hl=es', -33.3908877, -71.6823135
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'SUBIDA LOS LOBOS CON SAN FELIPE (CAMPANA MUNICIPAL)', 'ESQUINA EL BOSQUE', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.3909662%2C-71.6776473&z=17&hl=es', -33.3909662, -71.6776473
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'ORO NEGRO CON SAN FELIPE (CAMPANA MUNICIPAL)', 'ORO NEGRO C/ SAN FELIPE santa laura / san felipe', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.3935023%2C-71.6706283&z=17&hl=es', -33.3935023, -71.6706283
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'VILLA LAS MARINAS 2', 'AV. MONTEMAR HACIA LA DERECHA', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/gogP64z6T9yaPkLA8', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'VILLA LAS MARINAS 1', 'AV. MONTEMAR HACIA LA IZQUIERDA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.3878685%2C-71.6692907&z=17&hl=es', -33.3878685, -71.6692907
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'VILLA PADRE ALVEAR', 'ORO NEGRO. INTERIOR DEPARTAMENTOS', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/SpH5swnVbFT8TQFd7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'PLAZA LAS PALMAS', 'PINOMAR CON LAS PALMAS', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/WVJZuu1AEsTJiixW7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO ALTO Y PINOMAR', 'PLAZA LAS PALMAS', 'PINOMAR CON LAS PALMAS (Punto 2)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/WVJZuu1AEsTJiixW7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO NORTE', 'LA JOC (CAMPANA LIBERMART)', 'LA JOC CON AV. ISIDORO DUBOURNAIS', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/9ZQnvjg2yv8Zes3b7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO NORTE', '1 INICIO LAS BALANDRAS # 118 (CAMPANA MUNICIPAL)', 'AV. LAS BALANDRAS 118', 'MUNICIPAL', 1000.0, 'https://maps.app.goo.gl/2M4ajHcjKVStWJGF7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO NORTE', '2 LAS BALANDRAS ALTURA CONDOMINIO # 723 (CAMPANA MUNICIPAL)', 'LAS BALANDRAS 723', 'MUNICIPAL', 1000.0, 'https://maps.app.goo.gl/DUtQFZ4ooqHLjJEE6', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO NORTE', 'SEDE CORDILLERA (CAMPANA LIBERTMART)', 'MOPITA, ALCOSTADO DE LA SEDE', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/3bGe5bhCLWVyVmsA8', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO NORTE', 'PEDRO ALVAREZ SALAMANCA CON BELLAVISTA (CAMPANA MUNICIPAL)', 'PEDRO ALVAREZ SALAMANCA CON BELLAVISTA', 'MUNICIPAL', 1000.0, 'https://maps.app.goo.gl/5Vmb6Npp93iZE1oX6', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO NORTE', 'PEÑA GRIS CON LA PORTADA (CAMPANA MUNICIPAL)', 'PEÑA GRIS CON LA PORTADA', 'MUNICIPAL', 1000.0, 'https://maps.app.goo.gl/8mPsLdusJWemtPss6', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO NORTE', 'PLAZA BELLAVISTA (CAMPANA MUNICIPAL)', 'BELLAVISTA (CAMPANA GRANDE)', 'MUNICIPAL', 1000.0, 'https://maps.app.goo.gl/3tQvL3uAFjJRw1Su6', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO NORTE', 'PLAZA BELLAVISTA (CAMPANA LIBERTMART)', 'BELLAVISTA', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/3tQvL3uAFjJRw1Su6', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO NORTE', 'FRENTE RESTAURANT LA FRONTERA (CAMPANA MUNICIPAL)', 'AV. ISIDORO DUBOURNAIS', 'MUNICIPAL', 1000.0, 'https://maps.app.goo.gl/xzmygM2YmJhUiwgL7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO NORTE', 'FRENTE RESTAURANT LA FRONTERA (CAMPANA MUNICIPAL)', 'AV. ISIDORO DUBOURNAIS (Punto 2)', 'MUNICIPAL', 1000.0, 'https://maps.app.goo.gl/xzmygM2YmJhUiwgL7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'QUISCO NORTE', 'FRENTE RESTAURANT LA FRONTERA (CAMPANA MUNICIPAL)', 'AV. ISIDORO DUBOURNAIS (Punto 3)', 'MUNICIPAL', 1000.0, 'https://maps.app.goo.gl/xzmygM2YmJhUiwgL7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'El Quisco' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'CENTRO COMERCIAL (MUNICIPAL)', 'SUPERMERCADO CANELILLO BAHIA MANSA CN IGNACIO CARRERA PINTO', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.376861572265625%2C-71.68159484863281&z=17&hl=es', -33.376861572265625, -71.68159484863281
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'BOROA CON SANTA TERESITA', 'SANTA TERESITA BAHÍA MANSA', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/79uj5mkAAyWbtSyZA', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'PLAYA EL CANELO ESTACIONAMIENTO ESQUINA', 'PLAYA EL CANELILLO AGUA VERDE (EL CANELO)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.36970138549805%2C-71.68692779541016&z=17&hl=es', -33.36970138549805, -71.68692779541016
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'PLAYA EL CANELO ESTACIONAMIENTO ESQUINA', 'PLAYA EL CANELILLO AGUA VERDE (EL CANELO) (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.36970138549805%2C-71.68692779541016&z=17&hl=es', -33.36970138549805, -71.68692779541016
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'PLAYA EL CANELILLO ESTACIONAMIENTO', 'PLAYA EL CANELILLO (CANELILLO)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/ermozX6bhchETQx16', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'PLAYA EL CANELILLO ESTACIONAMIENTO', 'PLAYA EL CANELILLO (CANELILLO) (Punto 2)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/ermozX6bhchETQx16', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'LA RINCONADA CON GUACOLDA', 'LA RINCONADA CON GUACOLDA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.37053680419922%2C-71.67443084716797&z=17&hl=es', -33.37053680419922, -71.67443084716797
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'VÍA NAUTICA CN MAR DEL SUR (MUNICIPAL)', 'VÍA NAUTICA CN MAR DEL SUR', 'MUNICIPAL', 1000.0, 'https://maps.app.goo.gl/49XrGfKSmepnNT1JA', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'CONDOMINIO LOS LITRES DE ALGARROBO.', 'BALANDRA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.37919998168945%2C-71.6685562133789&z=17&hl=es', -33.37919998168945, -71.6685562133789
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'OSA MAYOR PORTAL DEL VIENTO', 'OSA MAYOR', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/kfsGTb7zr2VaSCiY7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'AVENIDA TOTORAL CON EL ESPINO (MUNICIPAL)', 'ALGARROBO CENTRO GILBERTO', 'MUNICIPAL', 1000.0, 'https://www.google.com/maps?q=-33.37252426147461,-71.6668930053711&z=17&hl=es', -33.37252426147461, -71.6668930053711
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'SUPERMERCADO LIDER (CAMPANA MUNICIPAL)', 'ROTONDA CAMINO ALGARROBO - EL QUISCO (CAMPANA GRANDE)', 'MUNICIPAL', 1000.0, 'https://maps.google.com/maps?q=-33.369110107421875%2C-71.66844177246094&z=17&hl=es', -33.369110107421875, -71.66844177246094
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'CESFAM ALGARROBO CARABINERO DE CHILE', 'AV. LOS CLAVELES CN CARABINEROS DE CHILE EX EL PINAR', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/JyoPc7NnQDqyGP7NA', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'CESFAM ALGARROBO CARABINERO DE CHILE', 'AV. LOS CLAVELES CN CARABINEROS DE CHILE EX EL PINAR (Punto 2)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/JyoPc7NnQDqyGP7NA', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'PLAZA LAS BRISAS', 'CALLE ALICIA M.A CON QUILLAYES', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.3687744140625%2C-71.66500091552734&z=17&hl=es', -33.3687744140625, -71.66500091552734
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'PLAZA LAS BRISAS', 'CALLE ALICIA M.A CON QUILLAYES (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.3687744140625%2C-71.66500091552734&z=17&hl=es', -33.3687744140625, -71.66500091552734
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'ALICIA MONCKEBERG CON PASAJE CARDENALES', 'CALLE ALICIA ( Inst: 19 - 5 ) (CIUDAD AZUL)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.37067413330078%2C-71.66389465332031&z=17&hl=es', -33.37067413330078, -71.66389465332031
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO SUR', 'ALICIA MONCKEBERG CON PASAJE LAAS GARZAS', 'AL FINAL ALICIA MONCKEBER', 'EMPRESA', 500.0, 'https://www.google.com/maps?q=-33.37325668334961,-71.66163635253906&z=17&hl=es', -33.37325668334961, -71.66163635253906
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO CENTRO', 'SANTA TERESA', 'SANTA TERESA', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/6UpbED5oaJACMe2o7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EJE AGUAS MARINAS', 'CAMINO A CASABLANCA AGUAS MARINAS', 'ALGARROBO CON JULIO HURTADO Cirilo Didier con Guillermo Schmidt (LA MESON)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.35795974731445%2C-71.65856170654297&z=17&hl=es', -33.35795974731445, -71.65856170654297
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EJE AGUAS MARINAS', 'CAMINO A CASABLANCA AGUAS MARINAS', 'ALGARROBO CON JULIO HURTADO Cirilo Didier con Guillermo Schmidt (LA MESON) (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.35795974731445%2C-71.65856170654297&z=17&hl=es', -33.35795974731445, -71.65856170654297
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EJE AGUAS MARINAS', 'AGUAS MARINAS', 'AGUAS MARINAS SUBIDA POR CORINA ARAVENA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.35929489135742%2C-71.6555404663086&z=17&hl=es', -33.35929489135742, -71.6555404663086
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'EJE AGUAS MARINAS', 'AGUAS MARINAS', 'AGUAS MARINAS SUBIDA POR CORINA ARAVENA (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.35929489135742%2C-71.6555404663086&z=17&hl=es', -33.35929489135742, -71.6555404663086
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO CENTRO', 'RESTAURANT LOS PATITOS', 'AV CARLOS ALESSANDRI', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/k4quEguTadLkVn3Q8', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO CENTRO', 'RESTAURANT LOS PATITOS', 'AV CARLOS ALESSANDRI (Punto 2)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/k4quEguTadLkVn3Q8', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO CENTRO', 'PLAZA EL OLIVAR', 'PUNTO NUEVO PUEBLO DE ARTESANOS', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/pZwPGPJkdEuiuE228', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO CENTRO', 'CONDOMINIO BOSQUES DE ALGARROBO', 'GRAN CAPITAN 236  PUNTO NUEVO', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/LiYda9wmLT1mp9q37', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO CENTRO', 'CONDOMINIO TORRES FRENTE SAN ALFONSO', 'ADENTRO CONDOMINIO ALTO DE SAN ALFONSO', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/RP68pVHUhadhAXa48', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO CENTRO', 'BRISAS DEL MAR', 'CAMINO MIRASOL (SACA)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/uQqzgXx8shvdqc789', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO CENTRO', 'CONDOMINIO SAN ALFONSO DEL MAR', 'SAN ALFONSO DEL MAR', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.364215%2C-71.668541&z=17&hl=es', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'MIRASOL', 'SAN JOSÉ CAMPANA MUNICIPAL', 'SAN JOSÉ', 'MUNICIPAL', 1000.0, 'https://maps.app.goo.gl/h8hDh7PdhhcdYQEc7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'TUNQUEN', 'CONDOMINIO CAMPOMAR', 'CAMINO TUNQUEN', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.29426193237305%2C-71.63074493408203&z=17&hl=es', -33.29426193237305, -71.63074493408203
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'TUNQUEN', 'CRUCE EL YECO', 'CAMINO TUNQUEN', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/8jJpmwHrHXq3P4uJ8', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'TUNQUEN', 'CRUCE EL YECO', 'CAMINO TUNQUEN (Punto 2)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/8jJpmwHrHXq3P4uJ8', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'TUNQUEN', 'CRUCE EL YECO (campana grande)', 'CAMINO TUNQUEN', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/8jJpmwHrHXq3P4uJ8', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'MIRASOL', 'PLAZA MIRASOL CALLE HEIMPELL', 'PLAZA MIRASOL (CAMPANA GRANDE)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/tdXEA1Vp24XcfToQ7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'MIRASOL', 'PLAZA MIRASOL CALLE HEIMPELL', 'PLAZA MIRASOL', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/tdXEA1Vp24XcfToQ7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'MIRASOL', 'PLAZA MIRASOL CALLE HEIMPELL', 'PLAZA MIRASOL NORTE PORTALES', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/KHNwHQR5cdXdUDzL8', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'MIRASOL', 'INICIO CAMINO DEL MEDIO', 'ESTADIO MIRASOL (CAMPANA GRANDE)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/uNfiDxGkSGYULa6P7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'MIRASOL', 'INICIO CAMINO DEL MEDIO', 'ESTADIO MIRASOL', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/uNfiDxGkSGYULa6P7', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'MIRASOL', 'RESTAURANT A TODA COSTA', 'ENTRAR X SAMUEL LILLO ( ORILLA PLAYA )', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/TWQxrqAzuNYhYoj7A', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO CENTRO', 'CENTRO ALGARROBO', 'CLUB DE YATES', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/BqjwS5AdXNWRf2MWA', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ALGARROBO CENTRO', 'CENTRO ALGARROBO', 'CLUB DE YATES (Punto 2)', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/BqjwS5AdXNWRf2MWA', -33.364215, -71.668541
FROM comunas WHERE nombre = 'Algarrobo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'CLUB DE GOLF', 'SANTO DOMINGO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.636600494384766%2C-71.61318969726562&z=17&hl=es', -33.636600494384766, -71.61318969726562
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'RESTAURANT SANTA PIZZA', 'PASEO  DEL MAR N° 200 SANTA MARIA DELMAR', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.6653938293457%2C-71.63819885253906&z=17&hl=es', -33.6653938293457, -71.63819885253906
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'MUNICIPALIDAD ROCAS STO.DOMINGO', 'FRENTE CENTRO COMERCIAL', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.64577865600586%2C-71.61355590820312&z=17&hl=es', -33.64577865600586, -71.61355590820312
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'MUNICIPALIDAD ROCAS STO.DOMINGO', 'FRENTE CENTRO COMERCIAL (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.64577865600586%2C-71.61355590820312&z=17&hl=es', -33.64577865600586, -71.61355590820312
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'MUNICIPALIDAD ROCAS STO.DOMINGO', 'FRENTE CENTRO COMERCIAL (Punto 3)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.64577865600586%2C-71.61355590820312&z=17&hl=es', -33.64577865600586, -71.61355590820312
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'MUNICIPALIDAD ROCAS STO.DOMINGO', 'FRENTE CENTRO COMERCIAL (Punto 4)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.64577865600586%2C-71.61355590820312&z=17&hl=es', -33.64577865600586, -71.61355590820312
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'MUNICIPALIDAD ROCAS STO.DOMINGO', 'FRENTE CENTRO COMERCIAL (Punto 5)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.64577865600586%2C-71.61355590820312&z=17&hl=es', -33.64577865600586, -71.61355590820312
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'MUNICIPALIDAD ROCAS STO.DOMINGO', 'FRENTE CENTRO COMERCIAL (Punto 6)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.64577865600586%2C-71.61355590820312&z=17&hl=es', -33.64577865600586, -71.61355590820312
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'MUNICIPALIDAD ROCAS STO.DOMINGO', 'FRENTE CENTRO COMERCIAL (Punto 7)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.64577865600586%2C-71.61355590820312&z=17&hl=es', -33.64577865600586, -71.61355590820312
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'MUNICIPALIDAD ROCAS STO.DOMINGO', 'FRENTE CENTRO COMERCIAL (Punto 8)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.64577865600586%2C-71.61355590820312&z=17&hl=es', -33.64577865600586, -71.61355590820312
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'COLEGIO EL ROBLE', 'CAMINO EL CONVENTO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.67138671875%2C-71.6019515991211&z=17&hl=es', -33.67138671875, -71.6019515991211
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'CAMPANA EL CONVENTO', 'EL CONVENTO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.770713806152344%2C-71.62056732177734&z=17&hl=es', -33.770713806152344, -71.62056732177734
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'CAMPANA EL CONVENTO', 'EL CONVENTO (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.770713806152344%2C-71.62056732177734&z=17&hl=es', -33.770713806152344, -71.62056732177734
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'CAMPANA EL CONVENTO', 'EL CONVENTO (Punto 3)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.770713806152344%2C-71.62056732177734&z=17&hl=es', -33.770713806152344, -71.62056732177734
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'LAS SALINAS DEL CONVENTO', 'EL CONVENTO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.784908294677734%2C-71.71540832519531&z=17&hl=es', -33.784908294677734, -71.71540832519531
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'LAS SALINAS DEL CONVENTO', 'EL CONVENTO (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.784908294677734%2C-71.71540832519531&z=17&hl=es', -33.784908294677734, -71.71540832519531
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'CONDOMINIO LAS BRISAS SANTO DOMINGO', 'CAMINO LA FRUTA', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.703521728515625%2C-71.6339111328125&z=17&hl=es', -33.703521728515625, -71.6339111328125
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'CONDOMINIO LAS BRISAS SANTO DOMINGO', 'CAMINO LA FRUTA (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.703521728515625%2C-71.6339111328125&z=17&hl=es', -33.703521728515625, -71.6339111328125
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'CONDOMINIO LAS BRISAS SANTO DOMINGO', 'CAMINO LA FRUTA (Punto 3)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.703521728515625%2C-71.6339111328125&z=17&hl=es', -33.703521728515625, -71.6339111328125
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'CLUB DE GOLF', 'CONDOMINIO LAS BRISAS SANTO DOMINGO', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.6963005065918%2C-71.64348602294922&z=17&hl=es', -33.6963005065918, -71.64348602294922
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'PUNTO LIMPIO CONSULTORIO', 'CALLE LA HORTENCIA  Las Hornillas Los Ruiseñores', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.6436767578125%2C-71.61203002929688&z=17&hl=es', -33.6436767578125, -71.61203002929688
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'PUNTO LIMPIO CONSULTORIO', 'CALLE LA HORTENCIA ( EX LOS CEREZOS ) Las Hornillas Los Ruiseñores', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.6436767578125%2C-71.61203002929688&z=17&hl=es', -33.6436767578125, -71.61203002929688
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'PLAZA LAURITA VICUÑA', 'SECTOR HORNILLA LOS MAITENES', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.64078903198242%2C-71.60870361328125&z=17&hl=es', -33.64078903198242, -71.60870361328125
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'PLAZA LAURITA VICUÑA', 'SECTOR HORNILLA LOS MAITENES (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.64078903198242%2C-71.60870361328125&z=17&hl=es', -33.64078903198242, -71.60870361328125
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'SECTOR BUCALEMU', 'CAMINO EL PORTAL', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.85527420043945%2C-71.66057586669922&z=17&hl=es', -33.85527420043945, -71.66057586669922
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'SECTOR BUCALEMU', 'CAMINO EL PORTAL (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.85527420043945%2C-71.66057586669922&z=17&hl=es', -33.85527420043945, -71.66057586669922
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'SAN ENRIQUE', 'CAMINO RAPEL', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.908084869384766%2C-71.71537017822266&z=17&hl=es', -33.908084869384766, -71.71537017822266
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'SAN ENRIQUE', 'CAMINO RAPEL (Punto 2)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.908084869384766%2C-71.71537017822266&z=17&hl=es', -33.908084869384766, -71.71537017822266
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'SAN ENRIQUE', 'CAMINO RAPEL (Punto 3)', 'EMPRESA', 500.0, 'https://maps.google.com/maps?q=-33.908084869384766%2C-71.71537017822266&z=17&hl=es', -33.908084869384766, -71.71537017822266
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;

INSERT INTO contenedores (comuna_id, sector, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud)
SELECT id, 'ROCAS SANTO DOMINGO', 'VIDRIOS Y ALUMINIOS DEL CONVENTO', 'Camino de servidumbre Prac 6-B LOTE 1 EL CONVENTO', 'EMPRESA', 500.0, 'https://maps.app.goo.gl/wPP7QMc5omCemkZs5', -33.6457786, -71.6135559
FROM comunas WHERE nombre = 'Santo Domingo' ON CONFLICT (comuna_id, nombre_punto, ubicacion_descripcion) DO NOTHING;
