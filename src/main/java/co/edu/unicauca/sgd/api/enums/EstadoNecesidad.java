package co.edu.unicauca.sgd.api.enums;

import co.edu.unicauca.sgd.api.utils.EnumUtils;

public enum EstadoNecesidad implements EnumUtils.ValorEnum {
    BORRADOR("BORRADOR"),
    EN_REVISION_SECRETARIO("EN REVISION SECRETARIO"),
    EN_REVISION_JEFE("EN REVISION JEFE"),
    NO_ASIGNADA("NO ASIGNADA"),
    ASIGNACION_NN("ASIGNACIÓN NN"),
    ASIGNADA("ASIGNADA"),
    GENERACION_HORARIOS("GENERACIÓN HORARIOS");

    private final String valor;

    EstadoNecesidad(String valor) { this.valor = valor; }

    @Override
    public String getValor() { return valor; }

    @Override
    public String toString() { return valor; }
}
