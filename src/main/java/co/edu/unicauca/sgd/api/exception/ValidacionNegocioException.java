package co.edu.unicauca.sgd.api.exception;

/**
 * Excepción utilizada para representar errores de validación o reglas de negocio incumplidas.
 */
public class ValidacionNegocioException extends RuntimeException {

    public ValidacionNegocioException(String mensaje) {
        super(mensaje);
    }
}
