package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.domain.ActividadBoolean;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

/**
 * Interfaz para definir los métodos del servicio de ActividadBoolean.
 */
public interface ActividadBooleanService {

    /**
     * Obtiene una lista paginada de registros en ACTIVIDADBOOLEAN.
     *
     * @param paginacion Configuración de paginación.
     * @return ApiResponse con la lista paginada de registros.
     */
    ResponseEntity<ApiResponse<Page<ActividadBoolean>>> obtenerTodos(int page, int size);

    /**
     * Obtiene un registro por su ID.
     *
     * @param id Identificador del registro.
     * @return ApiResponse con el registro encontrado.
     */
    ResponseEntity<ApiResponse<ActividadBoolean>> buscarPorId(Integer id);

    /**
     * Crea un nuevo registro en ACTIVIDADBOOLEAN.
     *
     * @param actividadBoolean Datos del nuevo registro.
     * @return ApiResponse con el registro creado.
     */
    ResponseEntity<ApiResponse<ActividadBoolean>> crear(ActividadBoolean actividadBoolean);

    /**
     * Actualiza un registro existente.
     *
     * @param id               Identificador del registro a actualizar.
     * @param actividadBoolean Datos actualizados del registro.
     * @return ApiResponse con el registro actualizado.
     */
    ResponseEntity<ApiResponse<ActividadBoolean>> actualizar(Integer id, ActividadBoolean actividadBoolean);

    /**
     * Elimina un registro por su ID.
     *
     * @param id Identificador del registro a eliminar.
     * @return ApiResponse con el resultado de la eliminación.
     */
    ResponseEntity<ApiResponse<Void>> eliminar(Integer id);
}
