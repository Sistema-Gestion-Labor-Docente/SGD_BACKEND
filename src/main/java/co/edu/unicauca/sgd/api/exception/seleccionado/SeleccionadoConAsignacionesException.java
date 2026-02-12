package co.edu.unicauca.sgd.api.exception.seleccionado;

import org.springframework.http.HttpStatus;

public class SeleccionadoConAsignacionesException extends SeleccionadoException {

    public SeleccionadoConAsignacionesException(Integer oidSeleccionado) {
        super(HttpStatus.CONFLICT,
                "No se puede eliminar el seleccionado " + oidSeleccionado + " porque tiene asignaciones asociadas.");
    }
}
