package co.edu.unicauca.sgd.api.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
