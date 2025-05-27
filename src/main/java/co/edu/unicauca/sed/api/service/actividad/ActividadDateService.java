package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.domain.ActividadDate;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

/**
 * Interfaz para definir los métodos del servicio de ActividadDate.
 */
public interface ActividadDateService {

    /**
     * Obtiene una lista paginada de registros en ACTIVIDADDATE.
     *
     * @param page Número de página.
     * @param size Tamaño de la página.
     * @return ApiResponse con la lista paginada de registros.
     */
    ResponseEntity<ApiResponse<Page<ActividadDate>>> obtenerTodos(int page, int size);

    /**
     * Obtiene un registro por su ID.
     *
     * @param id Identificador único de la actividad.
     * @return ApiResponse con la actividad encontrada.
     */
    ResponseEntity<ApiResponse<ActividadDate>> buscarPorId(Integer id);

    /**
     * Crea un nuevo registro en ACTIVIDADDATE.
     *
     * @param actividadDate Datos de la actividad a guardar.
     * @return ApiResponse con la actividad guardada.
     */
    ResponseEntity<ApiResponse<ActividadDate>> crear(ActividadDate actividadDate);

    /**
     * Actualiza una actividad existente en la base de datos.
     *
     * @param id            ID de la actividad a actualizar.
     * @param actividadDate Datos actualizados de la actividad.
     * @return ApiResponse con la actividad actualizada.
     */
    ResponseEntity<ApiResponse<ActividadDate>> actualizar(Integer id, ActividadDate actividadDate);

    /**
     * Elimina una actividad por su ID.
     *
     * @param id ID de la actividad a eliminar.
     * @return ApiResponse con el resultado de la eliminación.
     */
    ResponseEntity<ApiResponse<Void>> eliminar(Integer id);
}
