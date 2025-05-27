package co.edu.unicauca.sed.api.enums;

import co.edu.unicauca.sed.api.utils.EnumUtils;

public enum DepartamentoEnum implements EnumUtils.ValorEnum {
    ELECTRONICA_INSTRUMENTACION_CONTROL("DEPARTAMENTO DE ELECTRONICA INSTRUMENTACION Y CONTROL"),
    TELEMATICA("DEPARTAMENTO DE TELEMATICA"),
    TELECOMUNICACIONES("DEPARTAMENTO DE TELECOMUNICACIONES"),
    SISTEMAS("DEPARTAMENTO DE SISTEMAS");

    private final String valor;

    DepartamentoEnum(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }
}
