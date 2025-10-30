package co.edu.unicauca.sgd.api.service.materias.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.UsuarioDepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.UsuarioDepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.mapper.UsuarioDepartamentoMapper;
import co.edu.unicauca.sgd.api.repository.UsuarioActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.projection.UsuarioHorasProjection;
import co.edu.unicauca.sgd.api.service.materias.UsuarioDepartamentoService;
import jakarta.transaction.Transactional;

@Service
public class UsuarioDepartamentoServiceImpl implements UsuarioDepartamentoService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioDepartamentoServiceImpl.class);

    private final UsuarioDepartamentoRepository repository;

    private final UsuarioDepartamentoMapper mapper;

    private final UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository;

    public UsuarioDepartamentoServiceImpl(
            @Autowired UsuarioDepartamentoRepository repository,
            @Autowired UsuarioDepartamentoMapper mapper,
            @Autowired UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.usuarioActividadCalendarioRepository = usuarioActividadCalendarioRepository;
    }

    @Override
    @Transactional
    public ApiResponse<Page<UsuarioDepartamentoDTOResponse>> obtenerTodos(
            Integer oidUsuario, Integer oidDepartamento, Pageable pageable) {
        try {
            Specification<UsuarioDepartamento> spec = Specification.where(null);

            if (oidUsuario != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("oidUsuario"), oidUsuario));
            }
            if (oidDepartamento != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("departamento").get("oidDepartamento"), oidDepartamento));
            }

            Page<UsuarioDepartamento> page = repository.findAll(spec, pageable);

            Map<Integer, Float> horasPorUsuario = new HashMap<>();
            List<UsuarioDepartamento> contenido = page.getContent();
            if (!contenido.isEmpty()) {
                List<Integer> oidsUsuarios = contenido.stream()
                        .map(UsuarioDepartamento::getOidUsuario)
                        .collect(Collectors.toList());
                List<UsuarioHorasProjection> proyecciones = usuarioActividadCalendarioRepository
                        .sumarHorasPorUsuarios(oidsUsuarios);
                for (UsuarioHorasProjection proyeccion : proyecciones) {
                    horasPorUsuario.put(proyeccion.getOidUsuario(), proyeccion.getTotalHoras());
                }
            }

            Map<Integer, Float> horasMapFinal = horasPorUsuario;
            Page<UsuarioDepartamentoDTOResponse> response = page.map(entidad -> {
                UsuarioDepartamentoDTOResponse dto = mapper.toResponse(entidad);
                Float totalHoras = horasMapFinal.getOrDefault(entidad.getOidUsuario(), 0f);
                dto.setTotalHorasActividades(totalHoras);
                return dto;
            });

            logger.info("UsuarioDepartamento encontrados: {}", response.getTotalElements());
            return new ApiResponse<>(200, "Registros recuperados correctamente.", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al listar usuario-departamento: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioDepartamentoDTOResponse> buscarPorUsuario(Integer oidUsuario) {
        try {
            UsuarioDepartamento entity = repository.findById(oidUsuario)
                .orElseThrow(() -> new RuntimeException("No existe asignacion para el usuario: " + oidUsuario));
            UsuarioDepartamentoDTOResponse dto = mapper.toResponse(entity);
            Float totalHoras = usuarioActividadCalendarioRepository.sumarHorasPorUsuario(oidUsuario);
            dto.setTotalHorasActividades(totalHoras != null ? totalHoras : 0f);
            return new ApiResponse<>(200, "Asignacion encontrada correctamente.", dto);
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioDepartamentoDTOResponse> guardar(UsuarioDepartamentoDTORequest request) {
        try {
            // Como la PK es OIDUSUARIO, si ya existe devolvemos 400
            if (repository.existsById(request.getOidUsuario())) {
                return new ApiResponse<>(400, "El usuario ya tiene un departamento asignado.", null);
            }
            UsuarioDepartamento entity = mapper.convertToEntity(request);
            UsuarioDepartamento saved = repository.save(entity);
            logger.info("Usuario {} asignado a departamento {}", saved.getOidUsuario(),
                    saved.getDepartamento() != null ? saved.getDepartamento().getOidDepartamento() : null);
            return new ApiResponse<>(200, "Asignacion guardada correctamente.", mapper.toResponse(saved));
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar la asignacion: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioDepartamentoDTOResponse> actualizar(Integer oidUsuario, UsuarioDepartamentoDTORequest request) {
        try {
            UsuarioDepartamento existente = repository.findById(oidUsuario)
                .orElseThrow(() -> new RuntimeException("No existe asignacion para el usuario: " + oidUsuario));

            // Aseguramos consistencia: el path param manda
            request.setOidUsuario(oidUsuario);

            mapper.actualizarCamposBasicos(existente, request);
            UsuarioDepartamento actualizado = repository.save(existente);

            logger.info("Usuario {} reasignado al departamento {}",
                    actualizado.getOidUsuario(),
                    actualizado.getDepartamento() != null ? actualizado.getDepartamento().getOidDepartamento() : null);

            return new ApiResponse<>(200, "Asignacion actualizada correctamente.", mapper.toResponse(actualizado));
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, "Error en la actualizacion: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al actualizar: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oidUsuario) {
        try {
            if (!repository.existsById(oidUsuario)) {
                return new ApiResponse<>(404, "No existe asignacion para el usuario: " + oidUsuario, null);
            }
            repository.deleteById(oidUsuario);
            logger.info("Asignacion eliminada para el usuario {}", oidUsuario);
            return new ApiResponse<>(200, "Asignacion eliminada correctamente.", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar la asignacion: " + e.getMessage(), null);
        }
    }
}

