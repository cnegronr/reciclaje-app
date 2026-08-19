-- V5__add_rol_reporteria.sql: Soporte para el nuevo ROL REPORTERIA en la base de datos

-- Documentar e incorporar el rol REPORTERIA en la tabla usuarios
COMMENT ON COLUMN usuarios.rol IS 'Rol del usuario: INSPECTOR, CHOFER, ADMIN, REPORTERIA';
