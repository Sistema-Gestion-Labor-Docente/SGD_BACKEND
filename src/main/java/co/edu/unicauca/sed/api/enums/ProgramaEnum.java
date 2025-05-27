package co.edu.unicauca.sed.api.enums;

import co.edu.unicauca.sed.api.utils.EnumUtils;

public enum ProgramaEnum implements EnumUtils.ValorEnum {

    // Programas de Pregrado
    INGENIERIA_ELECTRONICA_Y_TELECOMUNICACIONES("INGENIERÍA ELECTRÓNICA Y TELECOMUNICACIONES"),
    INGENIERIA_DE_SISTEMAS("INGENIERÍA DE SISTEMAS"),
    INGENIERIA_EN_AUTOMATICA_INDUSTRIAL("INGENIERÍA EN AUTOMÁTICA INDUSTRIAL"),
    TECNOLOGIA_EN_TELEMATICA("TECNOLOGÍA EN TELEMÁTICA"),

    // Programas de Posgrado
    MAESTRIA_EN_COMPUTACION("MAESTRÍA EN COMPUTACIÓN"),
    MAESTRIA_EN_ELECTRONICA_Y_TELECOMUNICACIONES("MAESTRÍA EN ELECTRÓNICA Y TELECOMUNICACIONES"),
    MAESTRIA_EN_INGENIERIA_TELEMATICA("MAESTRÍA EN INGENIERÍA TELEMÁTICA"),
    MAESTRIA_EN_AUTOMATICA("MAESTRÍA EN AUTOMÁTICA"),
    DOCTORADO_EN_CIENCIAS_DE_LA_ELECTRONICA("DOCTORADO EN CIENCIAS DE LA ELECTRÓNICA"),

    OTRO("OTRO");

    private final String valor;

    ProgramaEnum(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }
}
