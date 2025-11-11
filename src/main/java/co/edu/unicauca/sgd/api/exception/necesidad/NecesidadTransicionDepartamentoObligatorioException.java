package co.edu.unicauca.sgd.api.exception.necesidad;

public class NecesidadTransicionDepartamentoObligatorioException extends NecesidadValidationException {

    public NecesidadTransicionDepartamentoObligatorioException() {
        super("El departamento es obligatorio para esta transición.");
    }
}
