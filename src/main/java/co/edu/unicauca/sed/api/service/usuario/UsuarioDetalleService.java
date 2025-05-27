package co.edu.unicauca.sed.api.service.usuario;

import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.domain.UsuarioDetalle;

import java.util.List;

/**
 * Interfaz para la gestión del servicio de detalles de usuario.
 */
public interface UsuarioDetalleService {

    /**
     * Obtiene todos los detalles de usuario almacenados.
     * 
     * @return Lista de detalles de usuario.
     */
    List<UsuarioDetalle> obtenerTodos();

    /**
     * Busca un detalle de usuario por su identificador único (OID).
     * 
     * @param oid Identificador del detalle de usuario.
     * @return UsuarioDetalle encontrado o null si no existe.
     */
    UsuarioDetalle buscarPorOid(Integer oid);

    /**
     * Guarda un nuevo detalle de usuario.
     * 
     * @param usuarioDetalle Datos del detalle de usuario a guardar.
     * @return UsuarioDetalle guardado.
     */
    UsuarioDetalle guardar(UsuarioDetalle usuarioDetalle);

    /**
     * Elimina un detalle de usuario por su identificador único.
     * 
     * @param oid Identificador del detalle de usuario.
     */
    void eliminar(Integer oid);

    /**
     * Procesa y normaliza los detalles de un usuario antes de almacenarlos.
     * 
     * @param usuario Usuario con los detalles a procesar.
     */
    void procesarUsuarioDetalle(Usuario usuario);
}
