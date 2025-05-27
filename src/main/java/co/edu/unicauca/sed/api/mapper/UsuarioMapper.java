package co.edu.unicauca.sed.api.mapper;

import co.edu.unicauca.sed.api.domain.Rol;
import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.domain.EstadoUsuario;
import co.edu.unicauca.sed.api.repository.EstadoUsuarioRepository;
import co.edu.unicauca.sed.api.repository.UsuarioRepository;
import co.edu.unicauca.sed.api.service.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * Clase encargada de mapear UsuarioDocenteDTO a Usuario.
 */
@Component
public class UsuarioMapper {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstadoUsuarioRepository estadoUsuarioRepository;

    @Autowired
    private RolService rolService;

    private static final List<String> ROLES_DEPARTAMENTO = List.of("JEFE DE DEPARTAMENTO");

    public void generarNombreUsuario(Usuario usuario) {
        if (usuario.getCorreo() != null && (usuario.getUsername() == null || usuario.getUsername().isEmpty())) {
            String correo = usuario.getCorreo();
            usuario.setUsername(correo.split("@")[0]);
        }
    }

    public void validarUsuarioExistente(Usuario usuario) {
        if (usuarioRepository.findByIdentificacion(usuario.getIdentificacion()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con la identificación: " + usuario.getIdentificacion());
        }
    }

    public void procesarEstadoUsuario(Usuario usuario) {
        if (usuario.getEstadoUsuario() != null && usuario.getEstadoUsuario().getOidEstadoUsuario() != null) {
            EstadoUsuario estadoUsuario = estadoUsuarioRepository
                .findById(usuario.getEstadoUsuario().getOidEstadoUsuario())
                .orElseThrow(() -> new RuntimeException("Estado Usuario no encontrado con OID: " + usuario.getEstadoUsuario().getOidEstadoUsuario()));
            usuario.setEstadoUsuario(estadoUsuario);
        }
    }

    public List<Rol> procesarRoles(Usuario usuario, Integer idUsuario) {
        List<Rol> rolesAsignados = rolService.processRoles(usuario.getRoles());
    
        for (Rol rol : rolesAsignados) {
            validarRolUnicoPorNombre(usuario, idUsuario, rol.getNombre().toUpperCase());
        }
    
        return rolesAsignados;
    }

    private void validarRolUnicoPorNombre(Usuario usuario, Integer idUsuario, String rolNombre) {
        if (List.of("JEFE DE DEPARTAMENTO", "DECANO", "SECRETARIA/O FACULTAD").contains(rolNombre)) {
            validarRolUnico(usuario, idUsuario, rolNombre);
        }
    }

    private void validarRolUnico(Usuario usuario, Integer idUsuario, String rolNombre) {
        List<String> rolBuscado = List.of(rolNombre.toUpperCase());
        String ubicacion = esRolDeDepartamento(rolNombre) ? "departamento" : "facultad";
        long count;
    
        if (esRolDeDepartamento(rolNombre)) {
            count = usuarioRepository.countByUsuarioDetalle_DepartamentoAndRoles_NombreInExcludingUser(
                usuario.getUsuarioDetalle().getDepartamento(), rolBuscado, idUsuario);
        } else {
            count = usuarioRepository.countByUsuarioDetalle_FacultadAndRoles_NombreInExcludingUser(
                usuario.getUsuarioDetalle().getFacultad(), rolBuscado, idUsuario);
        }
    
        if (count > 0) {
            throw new RuntimeException("Ya existe un " + rolNombre + " registrado en este " + ubicacion + ".");
        }
    }
    
    private boolean esRolDeDepartamento(String rolNombre) {
        return ROLES_DEPARTAMENTO.contains(rolNombre.toUpperCase());
    }

    public void validarCorreoExistente(String correo, Integer idActual) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
    
        if (usuarioOpt.isPresent()) {
            if (idActual == null || !usuarioOpt.get().getOidUsuario().equals(idActual)) {
                throw new RuntimeException("Ya existe un usuario registrado con el correo: " + correo);
            }
        }
    }

    public Optional<Usuario> obtenerUsuarioPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }
}
