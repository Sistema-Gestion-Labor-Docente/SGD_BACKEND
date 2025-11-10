package co.edu.unicauca.sgd.api.exception.seleccionado;

import org.springframework.http.HttpStatus;

public abstract class SeleccionadoException extends RuntimeException {

    private final HttpStatus status;

    protected SeleccionadoException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    protected SeleccionadoException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
