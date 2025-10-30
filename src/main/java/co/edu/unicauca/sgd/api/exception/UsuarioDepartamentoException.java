package co.edu.unicauca.sgd.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepcion base para errores relacionados con la gestion de usuario-departamento.
 */
public abstract class UsuarioDepartamentoException extends RuntimeException {

    private final HttpStatus status;

    protected UsuarioDepartamentoException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    protected UsuarioDepartamentoException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

