CREATE TABLE codigos_reserva
(
    id                  UUID PRIMARY KEY,
    capacitacion_id     UUID         NOT NULL UNIQUE,
    codigo_hash         VARCHAR(255) NOT NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_codigo_reserva_capacitacion
        FOREIGN KEY (capacitacion_id)
            REFERENCES capacitaciones (id)
);

CREATE TABLE asignaciones_participante
(
    id                   UUID PRIMARY KEY,
    capacitacion_id      UUID      NOT NULL,
    aula_capacitacion_id UUID      NOT NULL,
    personal_id          UUID      NOT NULL,
    fecha_asignacion     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activa               BOOLEAN   NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_asignacion_participante_capacitacion
        FOREIGN KEY (capacitacion_id)
            REFERENCES capacitaciones (id),
    CONSTRAINT fk_asignacion_participante_aula
        FOREIGN KEY (aula_capacitacion_id)
            REFERENCES aulas_capacitacion (id)
);

CREATE INDEX idx_asignacion_participante_capacitacion
    ON asignaciones_participante (capacitacion_id);

CREATE INDEX idx_asignacion_participante_aula
    ON asignaciones_participante (aula_capacitacion_id);

CREATE INDEX idx_asignacion_participante_personal
    ON asignaciones_participante (personal_id);

CREATE UNIQUE INDEX uk_participante_capacitacion_activa
    ON asignaciones_participante (capacitacion_id, personal_id) WHERE activa = TRUE;