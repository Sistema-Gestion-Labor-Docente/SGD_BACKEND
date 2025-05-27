package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.dto.FuenteDTO;
import co.edu.unicauca.sed.api.dto.actividad.ActividadPaginadaDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * Interfaz para la transformación de actividades y sus cálculos específicos.
 */
public interface ActividadTransformacionService {

    /**
     * Transforma una actividad en un mapa de datos con cálculos específicos.
     *
     * @param actividad    Actividad a transformar.
     * @param horasTotales Total de horas para calcular porcentajes.
     * @return Mapa con los datos de la actividad transformados.
     */
    Map<String, Object> transformarActividad(Actividad actividad, float horasTotales);

    /**
     * Transforma una lista de fuentes en una lista de DTOs.
     *
     * @param fuentes Lista de fuentes.
     * @return Lista de FuenteDTO con los datos transformados.
     */
    List<FuenteDTO> transformarFuentes(List<Fuente> fuentes);

    /**
     * Construye un objeto `ActividadPaginadaDTO` a partir de una página de actividades.
     *
     * @param actividadPage Página de actividades.
     * @return DTO con la información paginada de las actividades.
     */
    ActividadPaginadaDTO construirActividadPaginadaDTO(Page<Actividad> actividadPage);

    /**
     * Agrupa actividades por tipo y calcula el porcentaje de cada una.
     *
     * @param actividades Lista de actividades.
     * @param totalHoras  Total de horas de todas las actividades.
     * @return Mapa con las actividades agrupadas por tipo.
     */
    Map<String, List<Map<String, Object>>> agruparActividadesPorTipo(List<Actividad> actividades, float totalHoras);
}
