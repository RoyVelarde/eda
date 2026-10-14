CREATE TABLE personal
(
    id               UUID PRIMARY KEY,
    dni              VARCHAR(8)   NOT NULL UNIQUE,
    nivel            VARCHAR(20)  NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    nombres          VARCHAR(150) NOT NULL,
    celular          VARCHAR(15),
    correo           VARCHAR(150),
    cargo            VARCHAR(100) NOT NULL,
    situacion        VARCHAR(50),
    fecha_creacion   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_personal_nivel CHECK (nivel IN ('NIVEL_2', 'NIVEL_3'))
);

CREATE INDEX idx_personal_nivel ON personal (nivel);

CREATE INDEX idx_personal_apellidos ON personal (apellido_paterno, apellido_materno, nombres);