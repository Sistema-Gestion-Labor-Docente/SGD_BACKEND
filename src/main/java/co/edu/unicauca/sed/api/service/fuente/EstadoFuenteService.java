package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.EstadoFuente;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz para la gestión de EstadoFuente.
 */
public interface EstadoFuenteService {

    /**
     * Obtiene una lista paginada de EstadoFuente.
     *
     * @param pageable Configuración de paginación.
     * @return ApiResponse con la lista paginada.
     */
    ApiResponse<Page<EstadoFuente>> buscarTodos(Pageable pageable);

    /**
     * Busca un EstadoFuente por su ID.
     *
     * @param id ID del EstadoFuente.
     * @return ApiResponse con el EstadoFuente encontrado.
     */
    ApiResponse<EstadoFuente> buscarPorId(Integer id);

    /**
     * Crea un nuevo EstadoFuente.
     *
     * @param estadoFuente Objeto EstadoFuente a guardar.
     * @return ApiResponse con el EstadoFuente creado.
     */
    ApiResponse<EstadoFuente> crear(EstadoFuente estadoFuente);

    /**
     * Actualiza un EstadoFuente existente.
     *
     * @param id ID del EstadoFuente a actualizar.
     * @param estadoFuente Objeto EstadoFuente con los datos actualizados.
     * @return ApiResponse con el EstadoFuente actualizado.
     */
    ApiResponse<EstadoFuente> actualizar(Integer id, EstadoFuente estadoFuente);

    /**
     * Elimina un EstadoFuente por su ID.
     *
     * @param id ID del EstadoFuente a eliminar.
     * @return ApiResponse indicando el resultado de la operación.
     */
    ApiResponse<Void> eliminar(Integer id);

    public EstadoFuente createEstadoFuente(int oidEstado);
}
