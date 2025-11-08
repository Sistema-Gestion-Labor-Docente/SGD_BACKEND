package co.edu.unicauca.sgd.api.exception.calendario;

public class CalendarioNoEncontradoException extends CalendarioException {

    public CalendarioNoEncontradoException(Integer id) {
        super("Calendario no encontrado con ID: " + id);
    }
}
