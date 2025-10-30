package co.edu.unicauca.sgd.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza frente a errores inesperados en el flujo de usuario-departamento.
 */
public class UsuarioDepartamentoInternalException extends UsuarioDepartamentoException {

    public UsuarioDepartamentoInternalException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}

