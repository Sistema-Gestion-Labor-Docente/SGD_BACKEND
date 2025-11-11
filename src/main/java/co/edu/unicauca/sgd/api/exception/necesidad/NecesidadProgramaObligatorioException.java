package co.edu.unicauca.sgd.api.exception.necesidad;

public class NecesidadProgramaObligatorioException extends NecesidadValidationException {

    public NecesidadProgramaObligatorioException() {
        super("El programa es obligatorio para la búsqueda de necesidades.");
    }
}
