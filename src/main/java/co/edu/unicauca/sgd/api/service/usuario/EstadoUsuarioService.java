package co.edu.unicauca.sgd.api.service.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.domain.EstadoUsuario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;

public interface EstadoUsuarioService {

    /**
     * Crea un nuevo estado de usuario.
     * @param estadoUsuario Objeto EstadoUsuario a crear.
     * @return ApiResponse con el estado de usuario creado.
     */
    ApiResponse<EstadoUsuario> crear(EstadoUsuario estadoUsuario);

    /**
     * Busca un estado de usuario por su ID.
     * @param id Identificador del estado de usuario.
     * @return ApiResponse con el estado de usuario encontrado.
     */
    ApiResponse<EstadoUsuario> buscarPorId(Integer id);

    /**
     * Obtiene todos los estados de usuario con paginación.
     * @param pageable Configuración de la paginación.
     * @return ApiResponse con la lista paginada de estados de usuario.
     */
    ApiResponse<Page<EstadoUsuario>> buscarTodos(Pageable pageable);

    /**
     * Actualiza un estado de usuario existente.
     * @param id Identificador del estado de usuario.
     * @param estadoUsuario Datos actualizados del estado de usuario.
     * @return ApiResponse con el estado de usuario actualizado.
     */
    ApiResponse<EstadoUsuario> actualizar(Integer id, EstadoUsuario estadoUsuario);

    /**
     * Elimina un estado de usuario por su ID.
     * @param id Identificador del estado de usuario.
     * @return ApiResponse confirmando la eliminación.
     */
    ApiResponse<Void> eliminar(Integer id);
}
