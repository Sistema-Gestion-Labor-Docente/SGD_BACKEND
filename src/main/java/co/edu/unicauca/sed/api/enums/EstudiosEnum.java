package co.edu.unicauca.sed.api.enums;

import co.edu.unicauca.sed.api.utils.EnumUtils;

public enum EstudiosEnum implements EnumUtils.ValorEnum {
    MAESTRIA("MAESTRÍA"),
    DOCTORADO("DOCTORADO"),
    POSDOCTORADO("POSDOCTORADO"),
    PROFESIONAL("PROFESIONAL"),
    ESPECIALIZACION("ESPECIALIZACIÓN");

    private final String valor;

    EstudiosEnum(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }
}
