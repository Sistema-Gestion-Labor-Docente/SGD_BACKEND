package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.domain.ActividadDecimal;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

/**
 * Interfaz para definir los métodos del servicio de ActividadDecimal.
 */
public interface ActividadDecimalService {

    /**
     * Obtiene una lista paginada de registros en ACTIVIDADDECIMAL.
     *
     * @param page Número de página.
     * @param size Tamaño de la página.
     * @return ApiResponse con la lista paginada de registros.
     */
    ResponseEntity<ApiResponse<Page<ActividadDecimal>>> obtenerTodos(int page, int size);

    /**
     * Obtiene un registro por su ID.
     *
     * @param id Identificador único de la actividad.
     * @return ApiResponse con la actividad encontrada.
     */
    ResponseEntity<ApiResponse<ActividadDecimal>> buscarPorId(Integer id);

    /**
     * Crea un nuevo registro en ACTIVIDADDECIMAL.
     *
     * @param actividadDecimal Datos de la actividad a guardar.
     * @return ApiResponse con la actividad guardada.
     */
    ResponseEntity<ApiResponse<ActividadDecimal>> crear(ActividadDecimal actividadDecimal);

    /**
     * Actualiza una actividad existente en la base de datos.
     *
     * @param id               ID de la actividad a actualizar.
     * @param actividadDecimal Datos actualizados de la actividad.
     * @return ApiResponse con la actividad actualizada.
     */
    ResponseEntity<ApiResponse<ActividadDecimal>> actualizar(Integer id, ActividadDecimal actividadDecimal);

    /**
     * Elimina una actividad por su ID.
     *
     * @param id ID de la actividad a eliminar.
     * @return ApiResponse con el resultado de la eliminación.
     */
    ResponseEntity<ApiResponse<Void>> eliminar(Integer id);
}
