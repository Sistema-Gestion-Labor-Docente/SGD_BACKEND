package co.edu.unicauca.sed.api.enums;

import co.edu.unicauca.sed.api.utils.EnumUtils;

public enum CategoriaEnum implements EnumUtils.ValorEnum {
    ASOCIADO("ASOCIADO"),
    TITULAR("TITULAR"),
    AUXILIAR("AUXILIAR"),
    ASISTENTE("ASISTENTE"),
    NINGUNO("NINGUNO"),
    A("A"),
    B("B"),
    C("C"),
    D("D");

    private final String valor;

    CategoriaEnum(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        return valor;
    }
}
