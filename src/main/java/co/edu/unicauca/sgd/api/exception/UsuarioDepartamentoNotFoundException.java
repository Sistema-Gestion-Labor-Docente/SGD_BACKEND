package co.edu.unicauca.sgd.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando no se encuentra una asignacion usuario-departamento.
 */
public class UsuarioDepartamentoNotFoundException extends UsuarioDepartamentoException {

    public UsuarioDepartamentoNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}

