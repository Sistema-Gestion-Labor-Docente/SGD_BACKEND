package co.edu.unicauca.sgd.api.enums;

import co.edu.unicauca.sgd.api.utils.EnumUtils;

public enum FacultadEnum implements EnumUtils.ValorEnum {
    FIET("FACULTAD DE INGENIERÍA ELECTRÓNICA Y TELECOMUNICACIONES");

    private final String valor;

    FacultadEnum(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }
}
