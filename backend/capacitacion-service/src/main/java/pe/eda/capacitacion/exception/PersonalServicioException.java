package pe.eda.capacitacion.exception;

public class PersonalServicioException extends RuntimeException {
    public PersonalServicioException(String mensaje) {
        super(mensaje);
    }

    public PersonalServicioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
