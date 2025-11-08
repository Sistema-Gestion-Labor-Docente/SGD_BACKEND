package co.edu.unicauca.sgd.api.exception.calendario;

public class NombreFechaNoEncontradoException extends NombreFechaException {

    public NombreFechaNoEncontradoException(Integer id) {
        super("NombreFecha no encontrado con ID: " + id);
    }
}
