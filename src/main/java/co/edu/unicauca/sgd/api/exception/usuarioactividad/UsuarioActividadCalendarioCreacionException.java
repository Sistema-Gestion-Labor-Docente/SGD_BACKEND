package co.edu.unicauca.sgd.api.exception.usuarioactividad;

import org.springframework.http.HttpStatus;

public class UsuarioActividadCalendarioCreacionException extends UsuarioActividadCalendarioException {

    public UsuarioActividadCalendarioCreacionException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
