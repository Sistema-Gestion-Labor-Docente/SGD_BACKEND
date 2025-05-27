package co.edu.unicauca.sed.api.enums;

import co.edu.unicauca.sed.api.utils.EnumUtils;

public enum DedicacionEnum implements EnumUtils.ValorEnum {
    TIEMPO_COMPLETO("TIEMPO COMPLETO"),
    MEDIO_TIEMPO("MEDIO TIEMPO"),
    HORAS_CATEDRA("HORAS CÁTEDRA");

    private final String valor;

    DedicacionEnum(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }
}
