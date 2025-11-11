package co.edu.unicauca.sgd.api.exception.necesidad;

import org.springframework.http.HttpStatus;

public class NecesidadValidationException extends NecesidadException {

    public NecesidadValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public NecesidadValidationException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}
