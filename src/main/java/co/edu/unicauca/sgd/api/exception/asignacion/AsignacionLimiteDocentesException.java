package co.edu.unicauca.sgd.api.exception.asignacion;

import org.springframework.http.HttpStatus;

import co.edu.unicauca.sgd.api.exception.AsignacionException;

public class AsignacionLimiteDocentesException extends AsignacionException {

    public AsignacionLimiteDocentesException() {
        super(HttpStatus.CONFLICT, "La necesidad ya tiene el máximo de tres docentes asignados.");
    }
}
