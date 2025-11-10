package co.edu.unicauca.sgd.api.exception.seleccionado;

import org.springframework.http.HttpStatus;

public class SeleccionadoValidationException extends SeleccionadoException {

    public SeleccionadoValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public SeleccionadoValidationException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}
