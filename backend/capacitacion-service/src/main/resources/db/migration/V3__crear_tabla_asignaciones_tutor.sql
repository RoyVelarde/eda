CREATE TABLE asignaciones_tutor
(
    id                   UUID PRIMARY KEY,
    capacitacion_id      UUID        NOT NULL,
    aula_capacitacion_id UUID        NOT NULL,
    personal_id          UUID        NOT NULL,
    funcion              VARCHAR(20) NOT NULL,
    activa               BOOLEAN     NOT NULL DEFAULT TRUE,
    fecha_asignacion     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asignacion_tutor_capacitacion FOREIGN KEY (capacitacion_id) REFERENCES capacitaciones (id),
    CONSTRAINT fk_asignacion_tutor_aula FOREIGN KEY (aula_capacitacion_id) REFERENCES aulas_capacitacion (id),
    CONSTRAINT uk_tutor_aula_capacitacion UNIQUE (aula_capacitacion_id, personal_id),
    CONSTRAINT chk_asignacion_tutor_funcion CHECK (funcion IN ('RESPONSABLE', 'APOYO'))
);

CREATE INDEX idx_asignacion_tutor_capacitacion ON asignaciones_tutor (capacitacion_id);

CREATE INDEX idx_asignacion_tutor_aula ON asignaciones_tutor (aula_capacitacion_id);

CREATE INDEX idx_asignacion_tutor_personal ON asignaciones_tutor (personal_id);

CREATE INDEX idx_asignacion_tutor_funcion ON asignaciones_tutor (funcion);