package co.edu.unicauca.sgd.api.exception.seleccionado;

import org.springframework.http.HttpStatus;

public class SeleccionadoNotFoundException extends SeleccionadoException {

    public SeleccionadoNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
