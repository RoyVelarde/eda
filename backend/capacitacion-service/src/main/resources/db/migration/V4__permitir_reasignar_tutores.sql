ALTER TABLE asignaciones_tutor DROP CONSTRAINT uk_tutor_aula_capacitacion;

CREATE UNIQUE INDEX uk_tutor_aula_personal_activo
    ON asignaciones_tutor (aula_capacitacion_id, personal_id)
    WHERE activa = TRUE;