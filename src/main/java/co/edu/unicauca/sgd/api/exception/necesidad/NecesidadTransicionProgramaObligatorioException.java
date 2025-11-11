package co.edu.unicauca.sgd.api.exception.necesidad;

public class NecesidadTransicionProgramaObligatorioException extends NecesidadValidationException {

    public NecesidadTransicionProgramaObligatorioException() {
        super("El programa es obligatorio para esta transición.");
    }
}
