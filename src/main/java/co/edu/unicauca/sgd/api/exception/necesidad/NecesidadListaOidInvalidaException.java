package co.edu.unicauca.sgd.api.exception.necesidad;

public class NecesidadListaOidInvalidaException extends NecesidadValidationException {

    public NecesidadListaOidInvalidaException() {
        super("Los identificadores proporcionados no son válidos.");
    }
}
