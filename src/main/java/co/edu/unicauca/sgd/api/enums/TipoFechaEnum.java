package co.edu.unicauca.sgd.api.enums;

import co.edu.unicauca.sgd.api.utils.EnumUtils;

public enum TipoFechaEnum implements EnumUtils.ValorEnum {
    RESALTADAS("RESALTADAS"),
    NO_RESALTADAS("NO RESALTADAS"),
    ADMINISTRATIVAS("ADMINISTRATIVAS"),
    CLASES("CLASES"),
    OCASIONAL("OCASIONAL"),
    CATEDRA("CATEDRA"),
    PLANTA("PLANTA"),
    BECARIO_Y_PRACTICANTE("BECARIO Y PRACTICANTE"),
    PLANIFICACION("PLANIFICACION");

    private final String valor;

    TipoFechaEnum(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }

}
