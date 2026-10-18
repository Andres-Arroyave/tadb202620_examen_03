/*Examen No. 3
 Motor: MySQL
 Autor: Andres Arroyave Londoño
 ID SIGAA: 000547528
 */

-- -----------------------------------------------------------------------------
-- Entidades principales
-- -----------------------------------------------------------------------------

create table dragon
(
    id     char(36)     primary key not null default (UUID()),
    nombre varchar(100) not null unique,
    color  varchar(50)  not null,
    tamano varchar(50)  not null,
    constraint ck_dragon_nombre CHECK (char_LENGTH(TRIM(nombre)) > 0),
    constraint ck_dragon_color CHECK (char_LENGTH(TRIM(color)) > 0),
    constraint ck_dragon_tamano CHECK (char_LENGTH(TRIM(tamano)) > 0)
);

create table jinete
(
    id     char(36)     primary key not null default (UUID()),
    nombre varchar(100) not null unique,
    casa   varchar(100) not null,
    constraint ck_jinete_nombre CHECK (char_LENGTH(TRIM(nombre)) > 0),
    constraint ck_jinete_casa CHECK (char_LENGTH(TRIM(casa)) > 0)
);

create table guarida
(
    id        char(36)     primary key not null default (UUID()),
    nombre    varchar(100) not null unique,
    ubicacion varchar(150) not null,
    constraint uk_guarida_nombre unique (nombre),
    constraint ck_guarida_nombre CHECK (char_LENGTH(TRIM(nombre)) > 0),
    constraint ck_guarida_ubicacion CHECK (char_LENGTH(TRIM(ubicacion)) > 0)
);

-- -----------------------------------------------------------------------------
-- Tablas complementarias (relaciones muchos a muchos)
-- -----------------------------------------------------------------------------

-- Un jinete ha montado uno o muchos dragones; un dragón ha tenido uno o
-- muchos jinetes.
create table jinete_dragon
(
    id        char(36) primary key not null default (UUID()),
    jinete_id char(36) not null,
    dragon_id char(36) not null,
    constraint uk_jinete_dragon_par unique (jinete_id, dragon_id),
    constraint fk_jinete_dragon_jinete foreign key (jinete_id) references jinete (id),
    constraint fk_jinete_dragon_dragon foreign key (dragon_id) references dragon (id)
);

create table dragon_guarida
(
    id         char(36) primary key not null default (UUID()),
    dragon_id  char(36) not null,
    guarida_id char(36) not null,
    constraint uk_dragon_guarida_par unique (dragon_id, guarida_id),
    constraint fk_dragon_guarida_dragon foreign key (dragon_id) references dragon (id),
    constraint fk_dragon_guarida_guarida foreign key (guarida_id) references guarida (id)
);

-- -----------------------------------------------------------------------------
-- Índices de apoyo para las consultas cruzadas
-- (las UK compuestas ya cubren búsquedas por jinete_id y por dragon_id
--  respectivamente; se agregan los índices para el otro extremo)
-- -----------------------------------------------------------------------------

CREATE INDEX ix_jinete_dragon_dragon ON jinete_dragon (dragon_id);
CREATE INDEX ix_dragon_guarida_guarida ON dragon_guarida (guarida_id);
