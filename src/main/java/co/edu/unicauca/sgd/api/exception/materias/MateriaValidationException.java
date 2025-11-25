package co.edu.unicauca.sgd.api.exception.materias;

import org.springframework.http.HttpStatus;

/**
 * Excepción para errores de validación relacionados con búsquedas o manejo de materias.
 */
public class MateriaValidationException extends MateriasException {

    public MateriaValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public MateriaValidationException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}

