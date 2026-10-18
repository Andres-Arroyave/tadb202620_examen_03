/*Examen No. 3
 Motor: MySQL
 Autor: Andres Arroyave Londoño
 ID SIGAA: 000547528
 */

DELIMITER $$

-- -----------------------------------------------------------------------------
-- Procedimientos para DRAGON
-- -----------------------------------------------------------------------------

$$
CREATE PROCEDURE sp_dragon_insertar(
    IN  p_nombre VARCHAR(100),
    IN  p_color  VARCHAR(50),
    IN  p_tamano VARCHAR(50),
    OUT p_id     CHAR(36)
)
BEGIN
    SET p_id = UUID();
    INSERT INTO dragon (id, nombre, color, tamano)
    VALUES (p_id, p_nombre, p_color, p_tamano);
END$$

$$
CREATE PROCEDURE sp_dragon_actualizar(
    IN p_id     CHAR(36),
    IN p_nombre VARCHAR(100),
    IN p_color  VARCHAR(50),
    IN p_tamano VARCHAR(50)
)
BEGIN
    UPDATE dragon
    SET nombre = p_nombre,
        color  = p_color,
        tamano = p_tamano
    WHERE id = p_id;
END$$

$$
CREATE PROCEDURE sp_dragon_eliminar(
    IN p_id CHAR(36)
)
BEGIN
    DELETE FROM dragon
    WHERE id = p_id;
END$$

-- -----------------------------------------------------------------------------
-- Procedimientos para JINETE
-- -----------------------------------------------------------------------------

$$
CREATE PROCEDURE sp_jinete_insertar(
    IN  p_nombre VARCHAR(100),
    IN  p_casa   VARCHAR(100),
    OUT p_id     CHAR(36)
)
BEGIN
    SET p_id = UUID();
    INSERT INTO jinete (id, nombre, casa)
    VALUES (p_id, p_nombre, p_casa);
END$$

$$
CREATE PROCEDURE sp_jinete_actualizar(
    IN p_id     CHAR(36),
    IN p_nombre VARCHAR(100),
    IN p_casa   VARCHAR(100)
)
BEGIN
    UPDATE jinete
    SET nombre = p_nombre,
        casa   = p_casa
    WHERE id = p_id;
END$$

$$   
CREATE PROCEDURE sp_jinete_eliminar(
    IN p_id CHAR(36)
)
BEGIN
    DELETE FROM jinete
    WHERE id = p_id;
END$$

-- -----------------------------------------------------------------------------
-- Procedimientos para GUARIDA
-- -----------------------------------------------------------------------------

$$
CREATE PROCEDURE sp_guarida_insertar(
    IN  p_nombre    VARCHAR(100),
    IN  p_ubicacion VARCHAR(150),
    OUT p_id        CHAR(36)
)
BEGIN
    SET p_id = UUID();
    INSERT INTO guarida (id, nombre, ubicacion)
    VALUES (p_id, p_nombre, p_ubicacion);
END$$

$$
CREATE PROCEDURE sp_guarida_actualizar(
    IN p_id        CHAR(36),
    IN p_nombre    VARCHAR(100),
    IN p_ubicacion VARCHAR(150)
)
BEGIN
    UPDATE guarida
    SET nombre    = p_nombre,
        ubicacion = p_ubicacion
    WHERE id = p_id;
END$$

$$
CREATE PROCEDURE sp_guarida_eliminar(
    IN p_id CHAR(36)
)
BEGIN
    DELETE FROM guarida
    WHERE id = p_id;
END$$

DELIMITER ;
