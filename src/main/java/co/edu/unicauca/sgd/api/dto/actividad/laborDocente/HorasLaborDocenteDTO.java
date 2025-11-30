package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HorasLaborDocenteDTO {

    /**
     * Límite máximo de horas de labor docente por semana utilizado para calcular disponibilidad total.
     */
    public static final float HORAS_MAX_SEMANA = 40f;

    /**
     * Horas asignadas agrupadas por tipo de actividad.
     * La llave suele ser el nombre del tipo de actividad.
     */
    private Map<String, Float> horasAsignadasPorTipoActividad;

    /**
     * Total de horas asignadas (suma de todas las actividades).
     */
    private Float totalHorasAsignadas;

    /**
     * Horas disponibles agrupadas por tipo de actividad.
     */
    private Map<String, Float> horasDisponiblesPorTipoActividad;

    /**
     * Total de horas disponibles (suma de todas las actividades).
     */
    private Float totalHorasDisponibles;
}
