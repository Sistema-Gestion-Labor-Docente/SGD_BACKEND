package co.edu.unicauca.sgd.api.exception.necesidad;

public class NecesidadCalendarioObligatorioException extends NecesidadValidationException {

    public NecesidadCalendarioObligatorioException() {
        super("El calendario es obligatorio para la búsqueda de necesidades.");
    }
}
