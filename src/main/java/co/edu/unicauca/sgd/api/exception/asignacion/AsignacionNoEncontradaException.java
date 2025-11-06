package co.edu.unicauca.sgd.api.exception.asignacion;

import org.springframework.http.HttpStatus;

import co.edu.unicauca.sgd.api.exception.AsignacionException;

public class AsignacionNoEncontradaException extends AsignacionException {

    public AsignacionNoEncontradaException(Integer oidAsignacion) {
        super(HttpStatus.NOT_FOUND, "La asignación " + oidAsignacion + " no existe.");
    }
}
