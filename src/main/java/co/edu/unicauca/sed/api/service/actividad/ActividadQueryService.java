package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.Proceso;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sed.api.dto.actividad.ActividadDTOEvaluador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Interfaz para definir los métodos de consultas avanzadas sobre actividades.
 */
public interface ActividadQueryService {

    /**
     * Busca actividades donde un usuario es el evaluado.
     *
     * @param idEvaluador        ID del evaluador.
     * @param idEvaluado         ID del evaluado.
     * @param codigoActividad    Código de la actividad.
     * @param tipoActividad      Tipo de actividad.
     * @param nombreEvaluador    Nombre del evaluador.
     * @param roles              Lista de roles del usuario.
     * @param tipoFuente         Tipo de fuente.
     * @param estadoFuente       Estado de la fuente.
     * @param ordenAscendente    Indica si el orden es ascendente o descendente.
     * @param idPeriodoAcademico ID del período académico.
     * @param paginacion         Configuración de paginación.
     * @return ApiResponse con la lista de actividades en formato paginado.
     */
    ApiResponse<Page<ActividadBaseDTO>> buscarActividadesPorEvaluado(
            Integer idEvaluador, Integer idEvaluado, String codigoActividad, String tipoActividad,
            String nombreEvaluador, List<String> roles, String tipoFuente, String estadoFuente,
            Boolean ordenAscendente, Integer idPeriodoAcademico, Boolean asignacionDefault, Pageable paginacion);

    /**
     * Busca actividades donde un usuario es el evaluador.
     *
     * @param idEvaluador        ID del evaluador.
     * @param idEvaluado         ID del evaluado.
     * @param codigoActividad    Código de la actividad.
     * @param tipoActividad      Tipo de actividad.
     * @param nombreEvaluador    Nombre del evaluador.
     * @param roles              Lista de roles del usuario.
     * @param tipoFuente         Tipo de fuente.
     * @param estadoFuente       Estado de la fuente.
     * @param ordenAscendente    Indica si el orden es ascendente o descendente.
     * @param idPeriodoAcademico ID del período académico.
     * @param paginacion         Configuración de paginación.
     * @return ApiResponse con la lista de actividades en formato paginado.
     */
    ApiResponse<Page<ActividadDTOEvaluador>> buscarActividadesPorEvaluador(
        Integer evaluatorUserId, Integer evaluatedUserId, String activityCode, String activityType,
        String evaluatorName, List<String> roles, String sourceType, String sourceStatus,
        Boolean ascendingOrder, Integer idPeriodoAcademico, Boolean asignacionDefault, Pageable pageable);

    /**
     * Genera un filtro dinámico para la consulta de actividades.
     *
     * @param idEvaluador        ID del evaluador.
     * @param idEvaluado         ID del evaluado.
     * @param codigoActividad    Código de la actividad.
     * @param tipoActividad      Tipo de actividad.
     * @param nombreEvaluador    Nombre del evaluador.
     * @param roles              Lista de roles del usuario.
     * @param tipoFuente         Tipo de fuente.
     * @param estadoFuente       Estado de la fuente.
     * @param ordenAscendente    Ordenamiento ascendente/descendente.
     * @param idPeriodoAcademico ID del período académico.
     * @return Specification para la consulta dinámica.
     */
    Specification<Actividad> filtrarActividades(
            Integer idEvaluador, Integer idEvaluado, String codigoActividad, String tipoActividad,
            String nombreEvaluador, List<String> roles, String tipoFuente, String estadoFuente,
            Boolean ordenAscendente, Integer idPeriodoAcademico, Boolean asignacionDefault);

    /**
     * Obtiene actividades asociadas a procesos de forma paginada.
     *
     * @param procesos   Lista de procesos.
     * @param paginacion Configuración de paginación.
     * @return Página de actividades encontradas.
     */
    Page<Actividad> obtenerActividadesPorProcesosPaginadas(List<Proceso> procesos, Pageable paginacion);

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
