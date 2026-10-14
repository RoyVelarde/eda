CREATE TABLE aulas
(
    id               UUID PRIMARY KEY,
    codigo           VARCHAR(20)  NOT NULL UNIQUE,
    tipo             VARCHAR(20)  NOT NULL,
    capacidad        INTEGER      NOT NULL,
    activa           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_aula_tipo CHECK (tipo IN ('APLICADOR', 'ORIENTADOR')),
    CONSTRAINT chk_aula_capacidad CHECK (capacidad > 0)
);

CREATE INDEX idx_aulas_tipo ON aulas (tipo);

CREATE INDEX idx_aulas_activa ON aulas (activa);