package co.edu.unicauca.sgd.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando la peticion de usuario-departamento contiene datos invalidos.
 */
public class UsuarioDepartamentoValidationException extends UsuarioDepartamentoException {

    public UsuarioDepartamentoValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

