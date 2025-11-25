package co.edu.unicauca.sgd.api.exception.materias;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando un plan o un plan base no existe.
 */
public class PlanNotFoundException extends MateriasException {

    public PlanNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}

