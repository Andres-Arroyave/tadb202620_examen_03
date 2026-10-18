/*Examen No. 3
 Motor: MySQL
 Autor: Andres Arroyave Londoño
 ID SIGAA: 000547528
 */
-- 1. Creación de la base de datos
create database if not exists casa_dragon;

-- 2. Creación del usuario
create user if not exists 'casa_dragon_andres'@'%' identified by 'Dragon_Andres*';

-- 3. Asignación de privilegios mínimos para el usuario en la base de datos

-- Privilegios de conexión, creación de tablas, índices y modificaciones (DDL)
GRANT CREATE, ALTER, DROP, INDEX, REFERENCES, CREATE TEMPORARY TABLES ON casa_dragon.* TO 'casa_dragon_andres'@'%';
-- Privilegios para manipulación de datos (DML)
GRANT SELECT, INSERT, UPDATE, DELETE ON casa_dragon.* TO 'casa_dragon_andres'@'%';
-- Privilegios para vistas (requerido para la Etapa 3)
GRANT CREATE VIEW, SHOW VIEW ON casa_dragon.* TO 'casa_dragon_andres'@'%';
-- Privilegios para lógica almacenada (funciones, procedimientos y triggers)
GRANT CREATE ROUTINE, ALTER ROUTINE, EXECUTE, TRIGGER ON casa_dragon.* TO 'casa_dragon_andres'@'%';
-- 4. Aplicar cambios de privilegios
FLUSH PRIVILEGES;
