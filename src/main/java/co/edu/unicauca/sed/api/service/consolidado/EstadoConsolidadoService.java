package co.edu.unicauca.sed.api.service.consolidado;

import co.edu.unicauca.sed.api.domain.EstadoConsolidado;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

/**
 * Interfaz para definir los métodos del servicio de EstadoConsolidado.
 */
public interface EstadoConsolidadoService {

    /**
     * Obtiene una lista paginada de registros en EstadoConsolidado.
     *
     * @param page Número de página.
     * @param size Tamaño de la página.
     * @return ApiResponse con la lista paginada de registros.
     */
    ResponseEntity<ApiResponse<Page<EstadoConsolidado>>> obtenerTodos(int page, int size);

    /**
     * Obtiene un registro por su ID.
     *
     * @param id Identificador del registro.
     * @return ApiResponse con el registro encontrado.
     */
    ResponseEntity<ApiResponse<EstadoConsolidado>> buscarPorId(Integer id);

    /**
     * Crea un nuevo registro en EstadoConsolidado.
     *
     * @param estado Datos del nuevo registro.
     * @return ApiResponse con el registro creado.
     */
    ResponseEntity<ApiResponse<EstadoConsolidado>> crear(EstadoConsolidado estado);

    /**
     * Elimina un registro por su ID.
     *
     * @param id Identificador del registro a eliminar.
     * @return ApiResponse con el resultado de la eliminación.
     */
    ResponseEntity<ApiResponse<Void>> eliminar(Integer id);
}
