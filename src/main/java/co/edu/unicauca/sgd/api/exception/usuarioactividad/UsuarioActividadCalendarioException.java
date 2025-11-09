package co.edu.unicauca.sgd.api.exception.usuarioactividad;

import org.springframework.http.HttpStatus;

/**
 * Excepción base para operaciones sobre Usuario-Actividad-Calendario.
 */
public abstract class UsuarioActividadCalendarioException extends RuntimeException {

    private final HttpStatus status;

    protected UsuarioActividadCalendarioException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    protected UsuarioActividadCalendarioException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
