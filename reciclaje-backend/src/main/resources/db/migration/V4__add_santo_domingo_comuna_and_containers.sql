-- V4__add_santo_domingo_comuna_and_containers.sql: Migracion para agregar comuna Santo Domingo y 30 contenedores
-- Columna D -> nombre_punto, Columna E -> ubicacion_descripcion
-- Nota: Santo Domingo se crea sin asignacion de inspector por defecto (se asigna manualmente por Admin)

INSERT INTO comunas (nombre, codigo_region) VALUES ('Santo Domingo', 'V') ON CONFLICT (nombre) DO NOTHING;

-- Contenedores de Santo Domingo (Sector ROCAS SANTO DOMINGO, Categoria EMPRESA 500kg)
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
