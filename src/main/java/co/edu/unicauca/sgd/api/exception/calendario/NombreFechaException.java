package co.edu.unicauca.sgd.api.exception.calendario;

public class NombreFechaException extends RuntimeException {

    public NombreFechaException(String message) {
        super(message);
    }

    public NombreFechaException(String message, Throwable cause) {
        super(message, cause);
    }
}
