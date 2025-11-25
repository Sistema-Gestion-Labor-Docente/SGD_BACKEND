package co.edu.unicauca.sgd.api.exception.materias;

import org.springframework.http.HttpStatus;

/**
 * Excepción para errores de validación relacionados con documentos de planes.
 */
public class PlanDocumentoValidationException extends MateriasException {

    public PlanDocumentoValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public PlanDocumentoValidationException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}

