package co.edu.unicauca.sgd.api.service.usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.EstadoUsuario;
import co.edu.unicauca.sgd.api.domain.Rol;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.RolDTO;
import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTOResponse;
import co.edu.unicauca.sgd.api.mapper.DepartamentoMapper;
import co.edu.unicauca.sgd.api.mapper.ProgramaMapper;
import co.edu.unicauca.sgd.api.mapper.UsuarioMapper;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.EstadoUsuarioRepository;
import co.edu.unicauca.sgd.api.repository.ProgramaRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioRepository;
import co.edu.unicauca.sgd.api.service.actividad.Impl.ActividadDateServiceImpl;
import co.edu.unicauca.sgd.api.specification.UsuarioSpecification;

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

    private UsuarioRepository usuarioRepository;

    private EstadoUsuarioRepository estadoUsuarioRepository;

    private UsuarioMapper usuarioMapper;

    private UsuarioDetalleService usuarioDetalleService;

    private UsuarioDepartamentoRepository usuarioDepartamentoRepository;

    private ProgramaRepository programaRepository;

    private ProgramaMapper programaMapper;

    private DepartamentoRepository departamentoRepository;

    private DepartamentoMapper departamentoMapper;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
            EstadoUsuarioRepository estadoUsuarioRepository, UsuarioMapper usuarioMapper,
            UsuarioDetalleService usuarioDetalleService, UsuarioDepartamentoRepository usuarioDepartamentoRepository,
            ProgramaRepository programaRepository, ProgramaMapper programaMapper,
            DepartamentoRepository departamentoRepository, DepartamentoMapper departamentoMapper) {
        this.usuarioRepository = usuarioRepository;
        this.estadoUsuarioRepository = estadoUsuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.usuarioDetalleService = usuarioDetalleService;
        this.usuarioDepartamentoRepository = usuarioDepartamentoRepository;
        this.programaRepository = programaRepository;
        this.programaMapper = programaMapper;
        this.departamentoRepository = departamentoRepository;
        this.departamentoMapper = departamentoMapper;
    }

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
    public UsuarioDTO obtenerUsuarioActual(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con correo: " + correo));

        LOGGER.info("Obteniendo usuario actual: {}", usuario.getIdentificacion());

        // ===== Mapeo base a DTO =====
        UsuarioDTO dto = new UsuarioDTO();
        dto.setOidUsuario(usuario.getOidUsuario());
        dto.setIdentificacion(usuario.getIdentificacion());
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());

        List<RolDTO> rolesDto = usuario.getRoles() == null ? Collections.emptyList()
            : usuario.getRoles().stream()
                .map(r -> new RolDTO(r.getNombre()))
                .collect(Collectors.toList());
        dto.setRoles(rolesDto);

        // ===== Departamento al que pertenece el usuario (UsuarioDepartamento) =====
        usuarioDepartamentoRepository.findById(usuario.getOidUsuario()).ifPresent(ud -> {
            Departamento dep = ud.getDepartamento();
            DepartamentoDTOResponse depDto = new DepartamentoDTOResponse();
            depDto.setOidDepartamento(dep.getOidDepartamento());
            depDto.setNombre(dep.getNombre());
            depDto.setFacultad(dep.getFacultad());
            dto.setDepartamento(depDto);
        });

        // ===== Enriquecimiento según rol =====
        Set<String> nombresRol = rolesDto.stream()
            .map(r -> r.getNombre() == null ? "" : r.getNombre().trim().toUpperCase())
            .collect(Collectors.toSet());

        // COORDINADOR → programas que coordina
        if (nombresRol.contains("COORDINADOR")) {
            programaRepository.findByCoordinador_OidUsuario(usuario.getOidUsuario())
                .ifPresent(p -> {
                    ProgramaDTOResponse programaDTO = programaMapper.toResponse(p);
                    dto.setProgramaCoordinador(programaDTO);
                });
        } else {
            dto.setProgramaCoordinador(null);
        }

        // JEFE DE DEPARTAMENTO → departamentos donde es jefe
        if (nombresRol.contains("JEFE DE DEPARTAMENTO")) {
            departamentoRepository.findByJefe_OidUsuario(usuario.getOidUsuario())
                .ifPresent(dep -> {
                    DepartamentoDTOResponse depDto = departamentoMapper.toResponse(dep);
                    dto.setDepartamentoJefatura(depDto);
                });
        } else {
            dto.setDepartamentoJefatura(null);
        }

        LOGGER.info("Usuario actual obtenido: {}", dto.getIdentificacion());

        return dto;
    }
}
