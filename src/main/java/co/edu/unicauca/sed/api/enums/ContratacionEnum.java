package co.edu.unicauca.sed.api.enums;

import co.edu.unicauca.sed.api.utils.EnumUtils;

public enum ContratacionEnum implements EnumUtils.ValorEnum {
    PLANTA("PLANTA"),
    OCASIONAL("OCASIONAL"),
    BECARIOS_Y_PRACTICANTES("BECARIOS Y PRACTICANTES"),
    BECARIOS_POSTGRADO("BECARIO POSTGRADO"),
    CATEDRA("CÁTEDRA");

    private final String valor;

    ContratacionEnum(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }
}
