package co.edu.unicauca.sgd.api.exception.necesidad;

import org.springframework.http.HttpStatus;

public class NecesidadNotFoundException extends NecesidadException {

    public NecesidadNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
