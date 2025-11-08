package co.edu.unicauca.sgd.api.exception.asignacion;

import org.springframework.http.HttpStatus;

import co.edu.unicauca.sgd.api.exception.AsignacionException;

public class AsignacionDocenteDuplicadoException extends AsignacionException {

    public AsignacionDocenteDuplicadoException() {
        super(HttpStatus.CONFLICT, "El docente ya está asignado a esta necesidad.");
    }
}
