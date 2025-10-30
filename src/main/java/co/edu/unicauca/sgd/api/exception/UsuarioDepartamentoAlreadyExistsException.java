package co.edu.unicauca.sgd.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando se intenta crear una asignacion que ya existe.
 */
public class UsuarioDepartamentoAlreadyExistsException extends UsuarioDepartamentoException {

    public UsuarioDepartamentoAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

