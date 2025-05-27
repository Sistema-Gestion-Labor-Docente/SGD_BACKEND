package co.edu.unicauca.sed.api.service.actividad.Impl;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.service.actividad.ActividadCalculoService;
import co.edu.unicauca.sed.api.utils.MathUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio para cálculos de actividades.
 */
@Service
public class ActividadCalculoServiceImpl implements ActividadCalculoService {

    @Override
    public float calcularTotalHoras(List<Actividad> actividades) {
        return (float) actividades.stream()
                .mapToDouble(Actividad::getHoras)
                .sum();
    }

    @Override
    public double calcularPorcentaje(float horasActividad, float horasTotales) {
        if (horasTotales <= 0)
            return 0.0;

        return BigDecimal.valueOf((horasActividad / horasTotales) * 100)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public double calcularPromedio(List<Fuente> fuentes) {
        return MathUtils.redondearDecimal(
                fuentes.stream()
                    .filter(f -> f.getCalificacion() != null)
                    .mapToDouble(Fuente::getCalificacion)
                    .average()
                    .orElse(0),
                2).doubleValue();
    }

    @Override
    public double calcularAcumulado(double promedio, double porcentaje) {
        if (porcentaje <= 0) {
            return 0;
        }
        double acumulado = promedio * (porcentaje / 100);
        return Math.round(acumulado * 100.0) / 100.0;
    }

    @Override
    public double calcularTotalPorcentaje(Map<String, List<Map<String, Object>>> actividadesPorTipo) {
        double total = actividadesPorTipo.values().stream()
            .flatMap(List::stream)
            .mapToDouble(actividad -> ((Number) actividad.getOrDefault("porcentaje", 0)).doubleValue())
            .sum();
    
        return Math.min(Math.round(total), 100.0);
    }

    @Override
    public double calcularTotalAcumulado(Map<String, List<Map<String, Object>>> actividadesPorTipo) {
        return actividadesPorTipo.values().stream()
                .flatMap(List::stream)
                .mapToDouble(actividad -> ((Number) actividad.getOrDefault("acumulado", 0)).doubleValue())
                .sum();
    }
}
