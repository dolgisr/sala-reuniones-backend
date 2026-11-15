--liquibase formatted sql

--changeset sala-reuniones:002-crear-reservas
CREATE TYPE estado_reserva AS ENUM ('ACTIVA', 'CANCELADA');

CREATE TABLE reservas (
    id           UUID            PRIMARY KEY,
    sala_id      UUID            NOT NULL REFERENCES salas (id),
    responsable  VARCHAR(100)    NOT NULL,
    inicio       TIMESTAMP       NOT NULL,
    fin          TIMESTAMP       NOT NULL,
    estado       estado_reserva  NOT NULL,
    created_at   TIMESTAMPTZ     NOT NULL,
    updated_at   TIMESTAMPTZ     NOT NULL,
    deleted_at   TIMESTAMPTZ,
    CONSTRAINT ck_reservas_fin_despues_de_inicio CHECK (fin > inicio)
);

CREATE INDEX ix_reservas_sala_inicio ON reservas (sala_id, inicio);
--rollback DROP TABLE reservas;
--rollback DROP TYPE estado_reserva;
