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
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoAlreadyExistsException;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoException;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoInternalException;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoNotFoundException;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoValidationException;
import co.edu.unicauca.sgd.api.mapper.UsuarioDepartamentoMapper;
import co.edu.unicauca.sgd.api.repository.UsuarioActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.projection.UsuarioHorasProjection;
import co.edu.unicauca.sgd.api.service.materias.UsuarioDepartamentoService;
import jakarta.transaction.Transactional;

@Service
public class UsuarioDepartamentoServiceImpl implements UsuarioDepartamentoService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioDepartamentoServiceImpl.class);
    private static final String DOCENCIA = "DOCENCIA";

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
            boolean hasContent = response.hasContent();
            String message = hasContent ? "Registros recuperados correctamente." : "No se encontraron asignaciones usuario-departamento.";
            return new ApiResponse<>(200, message, response);
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error al listar usuario-departamento.", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioDepartamentoDTOResponse> buscarPorUsuario(Integer oidUsuario) {
        try {
            UsuarioDepartamento entity = repository.findById(oidUsuario)
                .orElseThrow(() -> new UsuarioDepartamentoNotFoundException("No existe asignacion para el usuario: " + oidUsuario));
            UsuarioDepartamentoDTOResponse dto = mapper.toResponse(entity);
            Float totalHoras = usuarioActividadCalendarioRepository.sumarHorasPorUsuario(oidUsuario);
            dto.setTotalHorasActividades(totalHoras != null ? totalHoras : 0f);
            return new ApiResponse<>(200, "Asignacion encontrada correctamente.", dto);
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error interno al buscar la asignacion.", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioDepartamentoDTOResponse> guardar(UsuarioDepartamentoDTORequest request) {
        try {
            if (request.getOidUsuario() == null || request.getOidDepartamento() == null) {
                throw new UsuarioDepartamentoValidationException("Los campos oidUsuario y oidDepartamento son obligatorios.");
            }
            if (repository.existsById(request.getOidUsuario())) {
                throw new UsuarioDepartamentoAlreadyExistsException("El usuario ya tiene un departamento asignado.");
            }
            UsuarioDepartamento entity = mapper.convertToEntity(request);
            UsuarioDepartamento saved = repository.save(entity);
            logger.info("Usuario {} asignado a departamento {}", saved.getOidUsuario(),
                    saved.getDepartamento() != null ? saved.getDepartamento().getOidDepartamento() : null);
            return new ApiResponse<>(200, "Asignacion guardada correctamente.", mapper.toResponse(saved));
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error al guardar la asignacion.", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<UsuarioDepartamentoDTOResponse> actualizar(Integer oidUsuario, UsuarioDepartamentoDTORequest request) {
        try {
            if (request.getOidDepartamento() == null) {
                throw new UsuarioDepartamentoValidationException("El campo oidDepartamento es obligatorio para la actualizacion.");
            }

            UsuarioDepartamento existente = repository.findById(oidUsuario)
                .orElseThrow(() -> new UsuarioDepartamentoNotFoundException("No existe asignacion para el usuario: " + oidUsuario));

            request.setOidUsuario(oidUsuario);

            mapper.actualizarCamposBasicos(existente, request);
            UsuarioDepartamento actualizado = repository.save(existente);

            logger.info("Usuario {} reasignado al departamento {}",
                    actualizado.getOidUsuario(),
                    actualizado.getDepartamento() != null ? actualizado.getDepartamento().getOidDepartamento() : null);

            return new ApiResponse<>(200, "Asignacion actualizada correctamente.", mapper.toResponse(actualizado));
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error interno al actualizar la asignacion.", e);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oidUsuario) {
        try {
            if (!repository.existsById(oidUsuario)) {
                throw new UsuarioDepartamentoNotFoundException("No existe asignacion para el usuario: " + oidUsuario);
            }
            repository.deleteById(oidUsuario);
            logger.info("Asignacion eliminada para el usuario {}", oidUsuario);
            return new ApiResponse<>(200, "Asignacion eliminada correctamente.", null);
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error al eliminar la asignacion.", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<List<UsuarioDepartamentoDTOResponse>> obtenerProfesoresPorTipoActividad(String filtro, Integer oidDepartamento) {
        try {
            if (oidDepartamento == null) {
                throw new UsuarioDepartamentoValidationException("El parámetro oidDepartamento es obligatorio.");
            }
            String filtroNormalizado = filtro == null ? DOCENCIA : filtro.trim().toUpperCase();

            List<UsuarioDepartamento> profesores;
            String mensajeVacio;
            String mensajeExitoso;

            switch (filtroNormalizado) {
                case "DOCENCIA":
                    profesores = repository.findProfesoresConTipoActividad(DOCENCIA, oidDepartamento);
                    mensajeVacio = "No se encontraron profesores con actividades de tipo DOCENCIA.";
                    mensajeExitoso = "Profesores con actividades de tipo DOCENCIA recuperados correctamente.";
                    break;
                case "NO_DOCENCIA":
                    profesores = repository.findProfesoresConTipoActividadDiferente(DOCENCIA, oidDepartamento);
                    mensajeVacio = "No se encontraron profesores con actividades diferentes a DOCENCIA.";
                    mensajeExitoso = "Profesores con actividades diferentes a DOCENCIA recuperados correctamente.";
                    break;
                default:
                    throw new UsuarioDepartamentoValidationException("Filtro inválido. Use DOCENCIA o NO_DOCENCIA.");
            }

            return construirRespuestaProfesores(profesores, mensajeVacio, mensajeExitoso);
        } catch (UsuarioDepartamentoException e) {
            throw e;
        } catch (Exception e) {
            throw new UsuarioDepartamentoInternalException("Error al listar profesores por tipo de actividad.", e);
        }
    }

    private ApiResponse<List<UsuarioDepartamentoDTOResponse>> construirRespuestaProfesores(
            List<UsuarioDepartamento> profesores,
            String mensajeVacio,
            String mensajeExitoso) {

        List<UsuarioDepartamentoDTOResponse> dtoList = profesores.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        if (!dtoList.isEmpty()) {
            Map<Integer, Float> horasPorUsuario = obtenerHorasPorUsuario(profesores);
            dtoList.forEach(dto -> {
                Integer oidUsuario = dto.getUsuario() != null ? dto.getUsuario().getOidUsuario() : null;
                if (oidUsuario != null) {
                    dto.setTotalHorasActividades(horasPorUsuario.getOrDefault(oidUsuario, 0f));
                }
            });
        }

        String mensaje = dtoList.isEmpty() ? mensajeVacio : mensajeExitoso;
        return new ApiResponse<>(200, mensaje, dtoList);
    }

    private Map<Integer, Float> obtenerHorasPorUsuario(List<UsuarioDepartamento> profesores) {
        List<Integer> oids = profesores.stream()
                .map(UsuarioDepartamento::getOidUsuario)
                .collect(Collectors.toList());

        Map<Integer, Float> horasPorUsuario = new HashMap<>();
        if (!oids.isEmpty()) {
            List<UsuarioHorasProjection> proyecciones = usuarioActividadCalendarioRepository.sumarHorasPorUsuarios(oids);
            for (UsuarioHorasProjection proyeccion : proyecciones) {
                horasPorUsuario.put(proyeccion.getOidUsuario(), proyeccion.getTotalHoras());
            }
        }
        return horasPorUsuario;
    }
}
