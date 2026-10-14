CREATE TABLE capacitaciones
(
    id UUID PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    anio INTEGER NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADA',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_capacitacion_anio CHECK (anio BETWEEN 2000 AND 2100),
    CONSTRAINT chk_capacitacion_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT chk_capacitacion_estado
        CHECK (
            estado IN (
                       'PLANIFICADA',
                       'EN_CURSO',
                       'FINALIZADA',
                       'CANCELADA'
                )
            )
);

CREATE INDEX idx_capacitaciones_anio ON capacitaciones (anio);

CREATE INDEX idx_capacitaciones_estado ON capacitaciones (estado);

CREATE INDEX idx_capacitaciones_fechas ON capacitaciones (fecha_inicio, fecha_fin);