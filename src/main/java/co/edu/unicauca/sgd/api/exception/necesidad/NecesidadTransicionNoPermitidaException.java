package co.edu.unicauca.sgd.api.exception.necesidad;

public class NecesidadTransicionNoPermitidaException extends NecesidadValidationException {

    public NecesidadTransicionNoPermitidaException() {
        super("Transición de estado no permitida.");
    }
}
