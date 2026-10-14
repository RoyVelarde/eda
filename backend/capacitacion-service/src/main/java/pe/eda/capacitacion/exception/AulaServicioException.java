package pe.eda.capacitacion.exception;

public class AulaServicioException extends RuntimeException {
    public AulaServicioException(String mensaje) {
        super(mensaje);
    }

    public AulaServicioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}