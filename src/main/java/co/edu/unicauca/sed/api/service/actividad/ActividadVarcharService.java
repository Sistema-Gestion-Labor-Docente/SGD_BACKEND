package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.domain.ActividadVarchar;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

/**
 * Interfaz para definir los métodos del servicio de ActividadVarchar.
 */
public interface ActividadVarcharService {

    /**
     * Obtiene una lista paginada de registros en ACTIVIDADVARCHAR.
     *
     * @param page Número de página.
     * @param size Tamaño de la página.
     * @return ApiResponse con la lista paginada de registros.
     */
    ResponseEntity<ApiResponse<Page<ActividadVarchar>>> obtenerTodos(int page, int size);

    /**
     * Busca un registro por su ID.
     *
     * @param id Identificador único de la actividad.
     * @return ApiResponse con la actividad encontrada.
     */
    ResponseEntity<ApiResponse<ActividadVarchar>> buscarPorId(Integer id);

    /**
     * Crea un nuevo registro en ACTIVIDADVARCHAR.
     *
     * @param actividadVarchar Datos de la actividad a guardar.
     * @return ApiResponse con la actividad guardada.
     */
    ResponseEntity<ApiResponse<ActividadVarchar>> crear(ActividadVarchar actividadVarchar);

    /**
     * Actualiza una actividad existente en la base de datos.
     *
     * @param id               ID de la actividad a actualizar.
     * @param actividadVarchar Datos actualizados de la actividad.
     * @return ApiResponse con la actividad actualizada.
     */
    ResponseEntity<ApiResponse<ActividadVarchar>> actualizar(Integer id, ActividadVarchar actividadVarchar);

    /**
     * Elimina una actividad por su ID.
     *
     * @param id ID de la actividad a eliminar.
     * @return ApiResponse con el resultado de la eliminación.
     */
    ResponseEntity<ApiResponse<Void>> eliminar(Integer id);
}
