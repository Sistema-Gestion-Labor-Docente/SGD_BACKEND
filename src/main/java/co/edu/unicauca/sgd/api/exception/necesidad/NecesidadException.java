package co.edu.unicauca.sgd.api.exception.necesidad;

import org.springframework.http.HttpStatus;

public abstract class NecesidadException extends RuntimeException {

    private final HttpStatus status;

    protected NecesidadException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    protected NecesidadException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
