package co.edu.unicauca.sgd.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Base para las excepciones específicas del flujo de asignaciones.
 */
public abstract class AsignacionException extends RuntimeException {

    private final HttpStatus status;

    protected AsignacionException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
