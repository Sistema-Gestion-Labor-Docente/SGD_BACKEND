package co.edu.unicauca.sgd.api.exception.calendario;

public class FechaException extends RuntimeException {

    public FechaException(String message) {
        super(message);
    }

    public FechaException(String message, Throwable cause) {
        super(message, cause);
    }
}
