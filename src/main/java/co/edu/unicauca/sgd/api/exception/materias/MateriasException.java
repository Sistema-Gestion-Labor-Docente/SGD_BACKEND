package co.edu.unicauca.sgd.api.exception.materias;

import org.springframework.http.HttpStatus;

/**
 * Excepción base para errores del dominio de materias/planes.
 */
public abstract class MateriasException extends RuntimeException {

    private final HttpStatus status;

    protected MateriasException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    protected MateriasException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

