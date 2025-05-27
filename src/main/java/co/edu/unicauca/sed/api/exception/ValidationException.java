package co.edu.unicauca.sed.api.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final int codigo;
    private final String detalle;

    public ValidationException(int codigo, String detalle) {
        super(detalle);
        this.codigo = codigo;
        this.detalle = detalle;
    }
}
