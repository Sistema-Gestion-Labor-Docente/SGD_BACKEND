package co.edu.unicauca.sgd.api.exception.asignacion;

import org.springframework.http.HttpStatus;

import co.edu.unicauca.sgd.api.exception.AsignacionException;

public class AsignacionCalendarioInvalidoException extends AsignacionException {

    public AsignacionCalendarioInvalidoException() {
        super(HttpStatus.BAD_REQUEST, "El seleccionado y la necesidad deben pertenecer al mismo calendario.");
    }
}