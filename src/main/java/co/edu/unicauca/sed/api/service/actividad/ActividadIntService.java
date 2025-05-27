package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.domain.ActividadInt;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

/**
 * Interfaz para definir los métodos del servicio de ActividadInt.
 */
public interface ActividadIntService {

    /**
     * Obtiene una lista paginada de registros en ACTIVIDADINT.
     *
     * @param page Número de página.
     * @param size Tamaño de la página.
     * @return ApiResponse con la lista paginada de registros.
     */
    ResponseEntity<ApiResponse<Page<ActividadInt>>> obtenerTodos(int page, int size);

    /**
     * Busca un registro por su ID.
     *
     * @param id Identificador único de la actividad.
     * @return ApiResponse con la actividad encontrada.
     */
    ResponseEntity<ApiResponse<ActividadInt>> buscarPorId(Integer id);

    /**
     * Crea un nuevo registro en ACTIVIDADINT.
     *
     * @param actividadInt Datos de la actividad a guardar.
     * @return ApiResponse con la actividad guardada.
     */
    ResponseEntity<ApiResponse<ActividadInt>> crear(ActividadInt actividadInt);

    /**
     * Actualiza una actividad existente en la base de datos.
     *
     * @param id               ID de la actividad a actualizar.
     * @param actividadInt Datos actualizados de la actividad.
     * @return ApiResponse con la actividad actualizada.
     */
    ResponseEntity<ApiResponse<ActividadInt>> actualizar(Integer id, ActividadInt actividadInt);

    /**
     * Elimina una actividad por su ID.
     *
     * @param id ID de la actividad a eliminar.
     * @return ApiResponse con el resultado de la eliminación.
     */
    ResponseEntity<ApiResponse<Void>> eliminar(Integer id);
}
