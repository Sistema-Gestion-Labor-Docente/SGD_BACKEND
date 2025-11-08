package co.edu.unicauca.sgd.api.exception.calendario;

public class FechaNoEncontradaException extends FechaException {

    public FechaNoEncontradaException(Integer id) {
        super("Fecha no encontrada con ID: " + id);
    }
}
