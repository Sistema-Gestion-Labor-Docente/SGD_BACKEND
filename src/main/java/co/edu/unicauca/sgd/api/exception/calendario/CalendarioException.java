package co.edu.unicauca.sgd.api.exception.calendario;

public class CalendarioException extends RuntimeException {

    public CalendarioException(String message) {
        super(message);
    }

    public CalendarioException(String message, Throwable cause) {
        super(message, cause);
    }
}
