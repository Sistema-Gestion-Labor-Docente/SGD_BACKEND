package co.edu.unicauca.sgd.api.exception;

/**
 * Excepción utilizada cuando una asignación de actividad supera el límite de horas permitido.
 */
public class AsignacionHorasExcedidasException extends RuntimeException {

    private final Integer oidUsuario;
    private final float limiteHoras;
    private final float horasAsignadas;
    private final float horasSolicitadas;

    public AsignacionHorasExcedidasException(Integer oidUsuario, float limiteHoras, float horasAsignadas, float horasSolicitadas) {
        super(String.format(
                "El usuario con identificador %d supera el máximo permitido de %.2f horas (actuales %.2f + nuevas %.2f).",
                oidUsuario,
                limiteHoras,
                horasAsignadas,
                horasSolicitadas));
        this.oidUsuario = oidUsuario;
        this.limiteHoras = limiteHoras;
        this.horasAsignadas = horasAsignadas;
        this.horasSolicitadas = horasSolicitadas;
    }

    public Integer getOidUsuario() {
        return oidUsuario;
    }

    public float getLimiteHoras() {
        return limiteHoras;
    }

    public float getHorasAsignadas() {
        return horasAsignadas;
    }

    public float getHorasSolicitadas() {
        return horasSolicitadas;
    }
}
