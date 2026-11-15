--liquibase formatted sql

--changeset sala-reuniones:001-crear-salas
CREATE TABLE salas (
    id          UUID         PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    deleted_at  TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_salas_nombre_activa ON salas (lower(nombre)) WHERE deleted_at IS NULL;
--rollback DROP TABLE salas;
