package pe.eda.capacitacion.enums;

import pe.eda.capacitacion.exception.ReglaNegocioException;

public enum FuncionParticipante {
    APLICADOR,
    ORIENTADOR;

    public static FuncionParticipante desdeTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(
                    "La función del participante es obligatoria."
            );
        }
        try {
            return FuncionParticipante.valueOf(
                    valor.trim().toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException(
                    "La función debe ser APLICADOR u ORIENTADOR."
            );
        }
    }
}