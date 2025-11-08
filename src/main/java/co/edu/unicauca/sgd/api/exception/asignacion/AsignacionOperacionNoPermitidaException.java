package co.edu.unicauca.sgd.api.exception.asignacion;

import org.springframework.http.HttpStatus;

import co.edu.unicauca.sgd.api.exception.AsignacionException;

public class AsignacionOperacionNoPermitidaException extends AsignacionException {

    public AsignacionOperacionNoPermitidaException() {
        super(HttpStatus.BAD_REQUEST, "No se permite cambiar la necesidad ni el seleccionado de una asignación existente.");
    }
}
