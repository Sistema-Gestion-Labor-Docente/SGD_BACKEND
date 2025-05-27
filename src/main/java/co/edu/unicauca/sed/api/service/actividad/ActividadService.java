package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.actividad.ActividadBaseDTO;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz para definir los métodos del servicio de actividad.
 */
public interface ActividadService {

    /**
     * Obtiene todas las actividades paginadas con opción de orden ascendente o
     * descendente.
     *
     * @param paginacion      Configuración de paginación.
     * @param ordenAscendente Define si el orden es ascendente o descendente.
     * @return ApiResponse con la lista paginada de actividades.
     */
    ApiResponse<Page<ActividadBaseDTO>> obtenerTodos(Pageable paginacion, Boolean ordenAscendente);

    /**
     * Busca una actividad por su ID.
     *
     * @param id Identificador único de la actividad.
     * @return La actividad encontrada o null si no existe.
     */
    Actividad buscarPorId(Integer id);

    /**
     * Busca una actividad y devuelve su DTO.
     *
     * @param id Identificador de la actividad.
     * @return ApiResponse con la actividad en formato DTO.
     */
    ApiResponse<ActividadBaseDTO> buscarDTOPorId(Integer id);

    /**
     * Guarda una nueva actividad en la base de datos.
     *
     * @param actividadDTO Datos de la actividad a guardar.
     * @return ApiResponse con la actividad guardada.
     */
    ApiResponse<List<Actividad>> guardar(List<ActividadBaseDTO> actividadesDTO);

    /**
     * Actualiza una actividad existente en la base de datos.
     *
     * @param idActividad  ID de la actividad a actualizar.
     * @param actividadDTO Datos actualizados de la actividad.
     * @return ApiResponse con la actividad actualizada.
     */
    ApiResponse<Actividad> actualizar(Integer idActividad, ActividadBaseDTO actividadDTO);

    /**
     * Elimina una actividad por su ID.
     *
     * @param id ID de la actividad a eliminar.
     * @return ApiResponse con el resultado de la eliminación.
     */
    ApiResponse<Void> eliminar(Integer id);
}
