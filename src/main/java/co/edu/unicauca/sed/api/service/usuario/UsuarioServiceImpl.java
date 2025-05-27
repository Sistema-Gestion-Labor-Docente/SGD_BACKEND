package co.edu.unicauca.sed.api.service.usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.unicauca.sed.api.domain.EstadoUsuario;
import co.edu.unicauca.sed.api.domain.Rol;
import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.mapper.UsuarioMapper;
import co.edu.unicauca.sed.api.repository.EstadoUsuarioRepository;
import co.edu.unicauca.sed.api.repository.UsuarioRepository;
import co.edu.unicauca.sed.api.service.actividad.Impl.ActividadDateServiceImpl;
import co.edu.unicauca.sed.api.specification.UsuarioSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Implementación del servicio de usuarios.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActividadDateServiceImpl.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstadoUsuarioRepository estadoUsuarioRepository;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private UsuarioDetalleService usuarioDetalleService;

    @Override
    public ApiResponse<Page<Usuario>> obtenerTodos(String identificacion, String nombre, String facultad,
            String departamento, String categoria, String contratacion, String dedicacion, String estudios, 
            String rol, String estado, String programa, Pageable pageable) {
        try {
            Page<Usuario> usuarios = usuarioRepository.findAll(
                UsuarioSpecification.byFilters(identificacion, nombre, facultad, departamento, categoria,
                    contratacion, dedicacion, estudios, rol, estado, programa),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),Sort.by("fechaCreacion").descending()));
            return new ApiResponse<>(200, "Usuarios encontrados correctamente.", usuarios);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al recuperar los usuarios: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Usuario> buscarPorId(Integer oid) {
        try {
            Usuario usuario = usuarioRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + oid));
            return new ApiResponse<>(200, "Usuario encontrado correctamente.", usuario);
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, "Usuario no encontrado: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al recuperar el usuario: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<List<Usuario>> guardar(List<Usuario> usuarios) {
        List<Usuario> usuariosGuardados = new ArrayList<>();
        List<String> errores = new ArrayList<>();
    
        for (Usuario usuario : usuarios) {
            try {
                usuarioMapper.validarUsuarioExistente(usuario);
                usuarioMapper.validarCorreoExistente(usuario.getCorreo(), null);
    
                usuario.setNombres(usuario.getNombres().toUpperCase());
                usuario.setApellidos(usuario.getApellidos().toUpperCase());
                usuarioMapper.generarNombreUsuario(usuario);
                usuarioDetalleService.procesarUsuarioDetalle(usuario);
                usuarioMapper.procesarEstadoUsuario(usuario);
                List<Rol> rolesAsignados = usuarioMapper.procesarRoles(usuario, null);
                usuario.setRoles(rolesAsignados);
    
                usuariosGuardados.add(usuarioRepository.save(usuario));
    
            } catch (RuntimeException e) {
                String mensajeError = String.format("Usuario con identificación %s no fue guardado: %s", usuario.getIdentificacion(), e.getMessage());
                LOGGER.warn(mensajeError);
                errores.add(mensajeError);
            } catch (Exception e) {
                String mensajeError = String.format("Error inesperado al guardar usuario %s: %s", usuario.getIdentificacion(), e.getMessage());
                LOGGER.error(mensajeError);
                errores.add(mensajeError);
            }
        }
    
        if (!usuariosGuardados.isEmpty()) {
            String mensaje = "Usuarios guardados correctamente.";
            if (!errores.isEmpty()) {
                mensaje += " Sin embargo, algunos usuarios no pudieron guardarse: " + String.join(" | ", errores);
            }
            return new ApiResponse<>(200, mensaje, usuariosGuardados);
        } else {
            String mensaje = "No se pudo guardar ningún usuario.";
            if (!errores.isEmpty()) {
                mensaje += " Detalles: " + String.join(" | ", errores);
            }
            return new ApiResponse<>(400, mensaje, null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Usuario> actualizar(Integer id, Usuario usuarioActualizado) {
        try {
            Usuario usuarioExistente = usuarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    
            if (!Objects.equals(usuarioActualizado.getIdentificacion(), usuarioExistente.getIdentificacion())) {
                usuarioMapper.validarUsuarioExistente(usuarioActualizado);
            }

            usuarioMapper.validarCorreoExistente(usuarioActualizado.getCorreo(), id);

            List<Rol> rolesAsignados = usuarioMapper.procesarRoles(usuarioActualizado, id);
            usuarioExistente.setRoles(rolesAsignados);
    
            usuarioExistente.setNombres(usuarioActualizado.getNombres().toUpperCase());
            usuarioExistente.setApellidos(usuarioActualizado.getApellidos().toUpperCase());
            usuarioExistente.setCorreo(usuarioActualizado.getCorreo());
            usuarioExistente.setIdentificacion(usuarioActualizado.getIdentificacion());
    
            if (usuarioActualizado.getEstadoUsuario() != null && usuarioActualizado.getEstadoUsuario().getOidEstadoUsuario() != null) {
                EstadoUsuario estadoUsuario = estadoUsuarioRepository.findById(usuarioActualizado.getEstadoUsuario().getOidEstadoUsuario())
                        .orElseThrow(() -> new RuntimeException("Estado Usuario no encontrado con OID: " + usuarioActualizado.getEstadoUsuario().getOidEstadoUsuario()));
                usuarioExistente.setEstadoUsuario(estadoUsuario);
            }
    
            usuarioDetalleService.procesarUsuarioDetalle(usuarioActualizado);
    
            Usuario usuarioGuardado = usuarioRepository.save(usuarioExistente);
            return new ApiResponse<>(200, "Usuario actualizado correctamente.", usuarioGuardado);
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, "Error en la actualización: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al actualizar el usuario: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!usuarioRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Usuario no encontrado con ID: " + oid, null);
            }
            usuarioRepository.deleteById(oid);
            return new ApiResponse<>(200, "Usuario eliminado correctamente.", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar el usuario: " + e.getMessage(), null);
        }
    }

    @Override
    public Usuario obtenerUsuarioActual(String correo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        Usuario usuario = usuarioOpt.orElseThrow(() -> new RuntimeException("Usuario no encontrado con correo: " + correo));

        return usuario;
    }
}
