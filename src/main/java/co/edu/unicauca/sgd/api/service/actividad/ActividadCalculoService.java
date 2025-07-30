package co.edu.unicauca.sgd.api.service.actividad;

import java.util.List;
import java.util.Map;

import co.edu.unicauca.sgd.api.domain.Actividad;

/**
 * Interface que define los métodos para cálculos de actividades.
 */
public interface ActividadCalculoService {

    /**
     * Calcula el total de horas de una lista de actividades.
     *
     * @param actividades Lista de actividades.
     * @return Total de horas.
     */
    float calcularTotalHoras(List<Actividad> actividades);

    /**
     * Calcula el porcentaje de horas de una actividad respecto al total.
     *
     * @param horasActividad Horas de la actividad.
     * @param horasTotales   Horas totales.
     * @return Porcentaje calculado redondeado a 2 decimales.
     */
    double calcularPorcentaje(float horasActividad, float horasTotales);

    /**
     * Calcula el valor acumulado de una actividad.
     *
     * @param promedio   Promedio de calificaciones.
     * @param porcentaje Porcentaje asociado.
     * @return Valor acumulado redondeado a 2 decimales.
     */
    double calcularAcumulado(double promedio, double porcentaje);

    /**
     * Calcula el total del porcentaje de actividades por tipo.
     *
     * @param actividadesPorTipo Mapa de actividades categorizadas por tipo.
     * @return Total del porcentaje.
     */
    double calcularTotalPorcentaje(Map<String, List<Map<String, Object>>> actividadesPorTipo);

    /**
     * Calcula el total acumulado de actividades por tipo.
     *
     * @param actividadesPorTipo Mapa de actividades categorizadas por tipo.
     * @return Total acumulado.
     */
    double calcularTotalAcumulado(Map<String, List<Map<String, Object>>> actividadesPorTipo);
}
