CREATE TABLE aulas_capacitacion
(
    id UUID PRIMARY KEY,
    capacitacion_id UUID NOT NULL,
    aula_id UUID NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aulas_capacitacion_capacitacion FOREIGN KEY (capacitacion_id) REFERENCES capacitaciones (id),
    CONSTRAINT uk_capacitacion_aula UNIQUE (capacitacion_id, aula_id)
);

CREATE INDEX idx_aulas_capacitacion_capacitacion ON aulas_capacitacion (capacitacion_id);

CREATE INDEX idx_aulas_capacitacion_aula ON aulas_capacitacion (aula_id);