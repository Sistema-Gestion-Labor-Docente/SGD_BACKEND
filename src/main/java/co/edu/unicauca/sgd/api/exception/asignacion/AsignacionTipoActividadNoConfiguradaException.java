package co.edu.unicauca.sgd.api.exception.asignacion;

import org.springframework.http.HttpStatus;

import co.edu.unicauca.sgd.api.exception.AsignacionException;

public class AsignacionTipoActividadNoConfiguradaException extends AsignacionException {

    public AsignacionTipoActividadNoConfiguradaException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "No se encontró el tipo de actividad 'DOCENCIA_DIRECTA'.");
    }
}
