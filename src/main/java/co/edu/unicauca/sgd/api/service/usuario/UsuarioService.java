package co.edu.unicauca.sgd.api.service.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.UsuarioDTO;

import java.util.List;

/**
 * Interfaz que define los métodos del servicio de usuarios.
 */
public interface UsuarioService {

    /**
     * Obtiene una lista de usuarios filtrados y paginados.
     */
    ApiResponse<Page<Usuario>> obtenerTodos(String identificacion, String nombre, String facultad, String departamento,
            String categoria, String contratacion, String dedicacion, String estudios, String rol, String estado,
            String programa, Pageable pageable);

    /**
     * Obtiene un usuario por su ID.
     */
    ApiResponse<Usuario> buscarPorId(Integer oid);

    /**
     * Guarda una lista de usuarios con sus detalles, roles y estado.
     */
    ApiResponse<List<Usuario>> guardar(List<Usuario> usuarios);

    /**
     * Actualiza un usuario existente con nuevos datos.
     */
    ApiResponse<Usuario> actualizar(Integer id, Usuario usuarioActualizado);

    /**
     * Elimina un usuario por su ID.
     */
    ApiResponse<Void> eliminar(Integer oid);

    UsuarioDTO obtenerUsuarioActual(String correo);
}
