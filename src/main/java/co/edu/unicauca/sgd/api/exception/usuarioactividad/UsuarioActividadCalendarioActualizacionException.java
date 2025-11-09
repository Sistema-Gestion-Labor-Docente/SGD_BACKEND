package co.edu.unicauca.sgd.api.exception.usuarioactividad;

import org.springframework.http.HttpStatus;

public class UsuarioActividadCalendarioActualizacionException extends UsuarioActividadCalendarioException {

    public UsuarioActividadCalendarioActualizacionException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
