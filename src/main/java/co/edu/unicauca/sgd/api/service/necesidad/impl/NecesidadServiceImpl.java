package co.edu.unicauca.sgd.api.service.necesidad.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTOResponse;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import co.edu.unicauca.sgd.api.mapper.NecesidadMapper;
import co.edu.unicauca.sgd.api.repository.CalendarioRepository;
import co.edu.unicauca.sgd.api.repository.MateriaRepository;
import co.edu.unicauca.sgd.api.repository.NecesidadRepository;
import co.edu.unicauca.sgd.api.service.necesidad.NecesidadService;

@Service
public class NecesidadServiceImpl implements NecesidadService {

    private static final Logger logger = LoggerFactory.getLogger(NecesidadServiceImpl.class);

    private final NecesidadRepository necesidadRepository;
    private final CalendarioRepository calendarioRepository;
    private final MateriaRepository materiaRepository;
    private final NecesidadMapper necesidadMapper;

    public NecesidadServiceImpl(NecesidadRepository necesidadRepository,
                                CalendarioRepository calendarioRepository,
                                MateriaRepository materiaRepository,
                                NecesidadMapper necesidadMapper) {
        this.necesidadRepository = necesidadRepository;
        this.calendarioRepository = calendarioRepository;
        this.materiaRepository = materiaRepository;
        this.necesidadMapper = necesidadMapper;
    }

    @Override
    public ApiResponse<Page<NecesidadDTOResponse>> obtenerTodos(Integer oidCalendario,
                                                                Integer idMateria,
                                                                EstadoNecesidad estado,
                                                                Pageable pageable) {
        Pageable pageableToUse = pageable != null ? pageable : Pageable.unpaged();

        try {
            Specification<Necesidad> specification = Specification.where(null);

            if (oidCalendario != null) {
                specification = specification.and((root, query, cb) ->
                        cb.equal(root.join("calendario").get("oidcalendario"), oidCalendario));
            }
            if (idMateria != null) {
                specification = specification.and((root, query, cb) ->
                        cb.equal(root.join("materia").get("idMateria"), idMateria));
            }
            if (estado != null) {
                specification = specification.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
            }

            Page<Necesidad> page = necesidadRepository.findAll(specification, pageableToUse);
            Page<NecesidadDTOResponse> response = page.map(necesidadMapper::toResponse);

            logger.info("Necesidades encontradas: {}", response.getTotalElements());
            String message = response.hasContent()
                    ? "Necesidades recuperadas correctamente."
                    : "No se encontraron necesidades.";
            return new ApiResponse<>(200, message, response);
        } catch (Exception e) {
            logger.error("Error al listar necesidades", e);
            return new ApiResponse<>(500, "Error al listar las necesidades: " + e.getMessage(), Page.empty(pageableToUse));
        }
    }

    @Override
    public ApiResponse<NecesidadDTOResponse> buscarPorId(Integer oid) {
        try {
            Necesidad necesidad = necesidadRepository.findById(oid)
                    .orElseThrow(() -> new IllegalStateException("Necesidad no encontrado con ID: " + oid));

            return new ApiResponse<>(200, "Necesidad encontrada correctamente.", necesidadMapper.toResponse(necesidad));
        } catch (IllegalStateException e) {
            logger.warn("Necesidad no encontrada: {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error al buscar necesidad", e);
            return new ApiResponse<>(500, "Error interno al buscar la necesidad: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<NecesidadDTOResponse> guardar(NecesidadDTORequest request) {
        try {
            Calendario calendario = validarCalendario(request.getOidCalendario());
            Materia materia = validarMateria(request.getIdMateria());

            if (necesidadRepository.existsByCalendario_OidcalendarioAndMateria_IdMateria(
                    calendario.getOidcalendario(), materia.getIdMateria())) {
                throw new RuntimeException("Ya existe una necesidad registrada para la materia en ese calendario.");
            }

            Necesidad entidad = necesidadMapper.toEntity(request);
            if (entidad.getEstado() == null) {
                entidad.setEstado(EstadoNecesidad.BORRADOR);
            }

            entidad.setCalendario(calendario);
            entidad.setMateria(materia);
            entidad.setCorrequisitoNecesidad(obtenerCorrequisito(calendario.getOidcalendario(), request.getCorrequisitoOidNecesidad()).orElse(null));
            entidad.setUsuarioCreacion("system");

            Necesidad guardada = necesidadRepository.save(entidad);
            logger.info("Necesidad guardada con ID: {}", guardada.getOidNecesidad());

            return new ApiResponse<>(201, "Necesidad guardada correctamente.", necesidadMapper.toResponse(guardada));
        } catch (RuntimeException e) {
            logger.warn("Error de validación al guardar necesidad: {}", e.getMessage());
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error interno al guardar necesidad", e);
            return new ApiResponse<>(500, "Error interno al guardar la necesidad: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<NecesidadDTOResponse> actualizar(Integer oid, NecesidadDTORequest request) {
        try {
            Necesidad existente = necesidadRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Necesidad no encontrada con ID: " + oid));

            if (request.getOidCalendario() != null && !request.getOidCalendario().equals(existente.getCalendario().getOidcalendario())) {
                throw new RuntimeException("El calendario no puede modificarse en una necesidad.");
            }

            if (request.getIdMateria() != null && !request.getIdMateria().equals(existente.getMateria().getIdMateria())) {
                Materia nuevaMateria = validarMateria(request.getIdMateria());
                if (necesidadRepository.existsByCalendario_OidcalendarioAndMateria_IdMateria(
                        existente.getCalendario().getOidcalendario(), nuevaMateria.getIdMateria())) {
                    throw new RuntimeException("Ya existe una necesidad registrada para la materia en ese calendario.");
                }
                existente.setMateria(nuevaMateria);
            }

            necesidadMapper.actualizarCampos(existente, request);

            if (request.getCorrequisitoOidNecesidad() != null) {
                Optional<Necesidad> correquisito = obtenerCorrequisito(existente.getCalendario().getOidcalendario(), request.getCorrequisitoOidNecesidad());
                existente.setCorrequisitoNecesidad(correquisito.orElse(null));
            }

            existente.setUsuarioActualizacion("system");
            Necesidad actualizada = necesidadRepository.save(existente);
            logger.info("Necesidad actualizada con ID: {}", oid);

            return new ApiResponse<>(200, "Necesidad actualizada correctamente.", necesidadMapper.toResponse(actualizada));
        } catch (RuntimeException e) {
            logger.warn("Error al actualizar necesidad: {}", e.getMessage());
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error interno al actualizar necesidad", e);
            return new ApiResponse<>(500, "Error interno al actualizar la necesidad: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!necesidadRepository.existsById(oid)) {
                throw new IllegalStateException("Necesidad no encontrado con ID: " + oid);
            }

            necesidadRepository.deleteById(oid);
            logger.info("Necesidad eliminada con ID: {}", oid);
            return new ApiResponse<>(204, "Necesidad eliminada correctamente.", null);
        } catch (IllegalStateException e) {
            logger.warn("Intento de eliminar necesidad inexistente: {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error al eliminar necesidad", e);
            return new ApiResponse<>(500, "Error al eliminar la necesidad: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Map<String, Object>> cambiarEstadoMasivo(Integer oidCalendario,
                                                                EstadoNecesidad estadoOrigen,
                                                                EstadoNecesidad estadoDestino,
                                                                Integer oidPrograma,
                                                                Integer oidDepartamento) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("calendario", oidCalendario);
        metadata.put("estadoOrigen", estadoOrigen != null ? estadoOrigen.name() : null);
        metadata.put("estadoDestino", estadoDestino != null ? estadoDestino.name() : null);
        if (oidPrograma != null) {
            metadata.put("oidPrograma", oidPrograma);
        }
        if (oidDepartamento != null) {
            metadata.put("oidDepartamento", oidDepartamento);
        }

        try {
            validarCalendario(oidCalendario);

            if (!esTransicionPermitida(estadoOrigen, estadoDestino)) {
                throw new RuntimeException("Transición de estado no permitida.");
            }

            if (requierePrograma(estadoOrigen, estadoDestino) && oidPrograma == null) {
                throw new RuntimeException("El programa es obligatorio para esta transición.");
            }

            if (requiereDepartamento(estadoOrigen, estadoDestino) && oidDepartamento == null) {
                throw new RuntimeException("El departamento es obligatorio para esta transición.");
            }

            List<Necesidad> necesidades = obtenerNecesidadesParaTransicion(
                    oidCalendario, estadoOrigen, oidPrograma, oidDepartamento);
            if (necesidades.isEmpty()) {
                metadata.put("totalNecesidades", 0);
                return new ApiResponse<>(404, "No se encontraron necesidades para la transición solicitada.", metadata);
            }

            boolean debeValidarDepartamento = EstadoNecesidad.EN_REVISION_JEFE.equals(estadoDestino);

            Map<String, List<Integer>> inconsistencias = validarRequisitosPrevios(necesidades, debeValidarDepartamento);
            if (!inconsistencias.getOrDefault("sinGrupo", List.of()).isEmpty()
                    || !inconsistencias.getOrDefault("sinCupo", List.of()).isEmpty()) {
                return new ApiResponse<>(400,
                        "Todas las necesidades deben tener grupo y cupo definidos antes de cambiar de estado.",
                        Map.of("inconsistencias", inconsistencias));
            }
            if (debeValidarDepartamento && !inconsistencias.getOrDefault("sinDepartamento", List.of()).isEmpty()) {
                return new ApiResponse<>(400,
                        "Cada necesidad debe tener un departamento asociado antes de pasar a EN REVISION JEFE.",
                        Map.of("inconsistencias", inconsistencias));
            }

            necesidades.forEach(necesidad -> {
                necesidad.setEstado(estadoDestino);
                necesidad.setUsuarioActualizacion("system");
            });
            necesidadRepository.saveAll(necesidades);

            metadata.put("totalNecesidades", necesidades.size());
            return new ApiResponse<>(200, "Estados actualizados correctamente.", metadata);
        } catch (RuntimeException e) {
            logger.warn("Error al cambiar estados masivamente: {}", e.getMessage());
            return new ApiResponse<>(400, e.getMessage(), metadata);
        } catch (Exception e) {
            logger.error("Error interno al cambiar estados masivamente", e);
            return new ApiResponse<>(500, "Error al cambiar el estado de las necesidades: " + e.getMessage(), metadata);
        }
    }

    private Calendario validarCalendario(Integer oidCalendario) {
        if (oidCalendario == null) {
            throw new RuntimeException("El calendario es obligatorio.");
        }
        return calendarioRepository.findById(oidCalendario)
                .orElseThrow(() -> new RuntimeException("Calendario no encontrado con ID: " + oidCalendario));
    }

    private Materia validarMateria(Integer idMateria) {
        if (idMateria == null) {
            throw new RuntimeException("La materia es obligatoria.");
        }
        return materiaRepository.findById(idMateria)
                .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + idMateria));
    }

    private Optional<Necesidad> obtenerCorrequisito(Integer oidCalendario, Integer correquisitoOid) {
        if (correquisitoOid == null) {
            return Optional.empty();
        }
        if (correquisitoOid <= 0) {
            return Optional.empty();
        }

        Necesidad correquisito = necesidadRepository.findById(correquisitoOid)
                .orElseThrow(() -> new RuntimeException("Correquisito no encontrado con ID: " + correquisitoOid));

        if (!correquisito.getCalendario().getOidcalendario().equals(oidCalendario)) {
            throw new RuntimeException("El correquisito debe pertenecer al mismo calendario.");
        }
        return Optional.of(correquisito);
    }

    private boolean esTransicionPermitida(EstadoNecesidad origen, EstadoNecesidad destino) {
        if (origen == null || destino == null) {
            return false;
        }
        return (EstadoNecesidad.BORRADOR.equals(origen) && EstadoNecesidad.EN_REVISION_SECRETARIO.equals(destino))
                || (EstadoNecesidad.EN_REVISION_SECRETARIO.equals(origen) && EstadoNecesidad.BORRADOR.equals(destino))
                || (EstadoNecesidad.EN_REVISION_SECRETARIO.equals(origen) && EstadoNecesidad.EN_REVISION_JEFE.equals(destino))
                || (EstadoNecesidad.EN_REVISION_JEFE.equals(origen) && EstadoNecesidad.EN_REVISION_SECRETARIO.equals(destino))
                || (EstadoNecesidad.EN_REVISION_JEFE.equals(origen) && EstadoNecesidad.NO_ASIGNADA.equals(destino));
    }

    private boolean requierePrograma(EstadoNecesidad origen, EstadoNecesidad destino) {
        return (EstadoNecesidad.BORRADOR.equals(origen) && EstadoNecesidad.EN_REVISION_SECRETARIO.equals(destino))
                || (EstadoNecesidad.EN_REVISION_SECRETARIO.equals(origen) && EstadoNecesidad.BORRADOR.equals(destino));
    }

    private boolean requiereDepartamento(EstadoNecesidad origen, EstadoNecesidad destino) {
        return EstadoNecesidad.EN_REVISION_JEFE.equals(origen) && EstadoNecesidad.NO_ASIGNADA.equals(destino);
    }

    private List<Necesidad> obtenerNecesidadesParaTransicion(Integer oidCalendario,
                                                             EstadoNecesidad estadoOrigen,
                                                             Integer oidPrograma,
                                                             Integer oidDepartamento) {
        List<Necesidad> necesidades = necesidadRepository.findAllByCalendario_OidcalendarioAndEstado(oidCalendario, estadoOrigen);

        return necesidades.stream()
                .filter(necesidad -> {
                    if (oidPrograma == null) {
                        return true;
                    }
                    Materia materia = necesidad.getMateria();
                    if (materia == null || materia.getPlan() == null || materia.getPlan().getPrograma() == null) {
                        return false;
                    }
                    return oidPrograma.equals(materia.getPlan().getPrograma().getOidPrograma());
                })
                .filter(necesidad -> {
                    if (oidDepartamento == null) {
                        return true;
                    }
                    Materia materia = necesidad.getMateria();
                    if (materia == null || materia.getDepartamento() == null) {
                        return false;
                    }
                    return oidDepartamento.equals(materia.getDepartamento().getOidDepartamento());
                })
                .collect(Collectors.toList());
    }

    private Map<String, List<Integer>> validarRequisitosPrevios(List<Necesidad> necesidades,
                                                                boolean validarDepartamento) {
        List<Integer> sinGrupo = necesidades.stream()
                .filter(necesidad -> necesidad.getGrupo() == null || necesidad.getGrupo().trim().isEmpty())
                .map(Necesidad::getOidNecesidad)
                .collect(Collectors.toList());

        List<Integer> sinCupo = necesidades.stream()
                .filter(necesidad -> necesidad.getCupo() == null || necesidad.getCupo() <= 0)
                .map(Necesidad::getOidNecesidad)
                .collect(Collectors.toList());

        Map<String, List<Integer>> inconsistencias = new HashMap<>();
        inconsistencias.put("sinGrupo", sinGrupo);
        inconsistencias.put("sinCupo", sinCupo);

        if (validarDepartamento) {
            List<Integer> sinDepartamento = necesidades.stream()
                    .filter(necesidad -> {
                        Materia materia = necesidad.getMateria();
                        return materia == null || materia.getDepartamento() == null
                                || materia.getDepartamento().getOidDepartamento() == null;
                    })
                    .map(Necesidad::getOidNecesidad)
                    .collect(Collectors.toList());
            inconsistencias.put("sinDepartamento", sinDepartamento);
        } else {
            inconsistencias.put("sinDepartamento", List.of());
        }
        return inconsistencias;
    }
}
