package co.edu.unicauca.sgd.api.service.actividad;

import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;

import java.util.List;

/**
 * Interfaz para definir los métodos de consultas avanzadas sobre actividades.
 */
public interface ActividadQueryService {

    /**
     * Ordena una lista de actividades por el nombre del tipo de actividad.
     *
     * @param actividades    Lista de actividades en formato DTO.
     * @param ascendingOrder Indica si el orden debe ser ascendente (true) o
     *                       descendente (false).
     * @return Lista de actividades ordenadas según el criterio especificado.
     */
    List<ActividadBaseDTO> ordenarActividadesPorTipo(List<ActividadBaseDTO> actividades, Boolean ascendingOrder);
}
