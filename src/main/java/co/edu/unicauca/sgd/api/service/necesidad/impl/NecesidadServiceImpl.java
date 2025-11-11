package co.edu.unicauca.sgd.api.service.necesidad.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadBulkCreateRequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTOResponse;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadCalendarioObligatorioException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadNotFoundException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadProgramaObligatorioException;
import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadValidationException;
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
                                                                Integer oidPrograma,
                                                                Integer oidDepartamento,
                                                                Pageable pageable) {
        Pageable pageableToUse = pageable != null ? pageable : Pageable.unpaged();

        try {
            validarParametrosListado(oidCalendario, oidPrograma);

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
            if (oidPrograma != null) {
                specification = specification.and((root, query, cb) ->
                        cb.equal(root.join("materia").join("plan").join("programa").get("oidPrograma"), oidPrograma));
            }
            if (oidDepartamento != null) {
                specification = specification.and((root, query, cb) ->
                        cb.equal(root.join("materia").join("departamento").get("oidDepartamento"), oidDepartamento));
            }

            Page<Necesidad> page = necesidadRepository.findAll(specification, pageableToUse);
            Page<NecesidadDTOResponse> response = page.map(necesidadMapper::toResponse);

            logger.info("Necesidades encontradas: {}", response.getTotalElements());
            String message = response.hasContent()
                    ? "Necesidades recuperadas correctamente."
                    : "No se encontraron necesidades.";
            return new ApiResponse<>(200, message, response);
        } catch (NecesidadException e) {
            logger.warn("Validación de listado de necesidades: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), Page.empty(pageableToUse));
        } catch (Exception e) {
            logger.error("Error al listar necesidades", e);
            return new ApiResponse<>(500, "Error al listar las necesidades: " + e.getMessage(), Page.empty(pageableToUse));
        }
    }

    @Override
    public ApiResponse<NecesidadDTOResponse> buscarPorId(Integer oid) {
        try {
            Necesidad necesidad = necesidadRepository.findById(oid)
                    .orElseThrow(() -> new NecesidadNotFoundException("Necesidad no encontrada con ID: " + oid));

            return new ApiResponse<>(200, "Necesidad encontrada correctamente.", necesidadMapper.toResponse(necesidad));
        } catch (NecesidadNotFoundException e) {
            logger.warn("Necesidad no encontrada: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
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
            String grupoNormalizado = normalizarGrupo(request.getGrupo());

            if (grupoNormalizado == null || grupoNormalizado.isBlank()) {
                throw new NecesidadValidationException("El grupo es obligatorio para la necesidad.");
            }

            if (necesidadRepository.existsByCalendario_OidcalendarioAndMateria_IdMateriaAndGrupo(
                    calendario.getOidcalendario(), materia.getIdMateria(), grupoNormalizado)) {
                throw new NecesidadValidationException(String.format(
                        "Ya existe una necesidad registrada para la materia %s en el grupo %s.",
                        materia.getNombre(), grupoNormalizado));
            }

            Necesidad entidad = necesidadMapper.toEntity(request);
            if (entidad.getEstado() == null) {
                entidad.setEstado(EstadoNecesidad.BORRADOR);
            }

            entidad.setCalendario(calendario);
            entidad.setMateria(materia);
            entidad.setGrupo(grupoNormalizado);
            entidad.setCorrequisitoNecesidad(obtenerCorrequisito(calendario.getOidcalendario(), request.getCorrequisitoOidNecesidad()).orElse(null));
            entidad.setUsuarioCreacion("system");

            Necesidad guardada = necesidadRepository.save(entidad);
            logger.info("Necesidad guardada con ID: {}", guardada.getOidNecesidad());

            return new ApiResponse<>(201, "Necesidad guardada correctamente.", necesidadMapper.toResponse(guardada));
        } catch (NecesidadException e) {
            logger.warn("Error al guardar necesidad: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error interno al guardar necesidad", e);
            return new ApiResponse<>(500, "Error interno al guardar la necesidad: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<List<NecesidadDTOResponse>> guardarMasivo(NecesidadBulkCreateRequest request) {
        try {
            if (request == null || request.getNecesidades() == null || request.getNecesidades().isEmpty()) {
                throw new NecesidadValidationException("Debe enviar al menos una materia para crear necesidades.");
            }

            Calendario calendario = validarCalendario(request.getOidCalendario());
            List<NecesidadDTOResponse> creadas = new ArrayList<>();
            Set<Integer> materiasUnicas = new HashSet<>();

            for (NecesidadBulkCreateRequest.NecesidadBulkItemRequest item : request.getNecesidades()) {
                if (!materiasUnicas.add(item.getIdMateria())) {
                    throw new NecesidadValidationException(
                            "La materia con ID " + item.getIdMateria() + " está duplicada en la solicitud.");
                }
            }

            for (NecesidadBulkCreateRequest.NecesidadBulkItemRequest item : request.getNecesidades()) {
                Materia materia = validarMateria(item.getIdMateria());

                Set<String> gruposExistentes = necesidadRepository
                        .findAllByCalendario_OidcalendarioAndMateria_IdMateria(
                                calendario.getOidcalendario(), materia.getIdMateria())
                        .stream()
                        .map(Necesidad::getGrupo)
                        .filter(grupo -> grupo != null && !grupo.isBlank())
                        .map(this::normalizarGrupo)
                        .collect(Collectors.toCollection(HashSet::new));

                int gruposCreados = 0;
                int indiceGrupo = 0;
                while (gruposCreados < item.getCantidadGrupos()) {
                    String grupoGenerado = generarNombreGrupo(indiceGrupo++);
                    if (gruposExistentes.contains(grupoGenerado)) {
                        continue;
                    }
                    gruposExistentes.add(grupoGenerado);

                    Necesidad necesidad = new Necesidad();
                    necesidad.setCalendario(calendario);
                    necesidad.setMateria(materia);
                    necesidad.setGrupo(grupoGenerado);
                    necesidad.setCupo(item.getCupo());
                    necesidad.setEstado(EstadoNecesidad.BORRADOR);
                    necesidad.setUsuarioCreacion("system");

                    Necesidad guardada = necesidadRepository.save(necesidad);
                    creadas.add(necesidadMapper.toResponse(guardada));
                    gruposCreados++;
                }
            }

            logger.info("Necesidades creadas masivamente: {}", creadas.size());
            return new ApiResponse<>(201, "Necesidades creadas correctamente.", creadas);
        } catch (NecesidadException e) {
            logger.warn("Error al crear necesidades masivamente: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error interno al crear necesidades masivamente", e);
            return new ApiResponse<>(500, "Error interno al crear las necesidades: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<NecesidadDTOResponse> actualizar(Integer oid, NecesidadDTORequest request) {
        try {
            Necesidad existente = necesidadRepository.findById(oid)
                    .orElseThrow(() -> new NecesidadNotFoundException("Necesidad no encontrada con ID: " + oid));

            if (request.getOidCalendario() != null
                    && !request.getOidCalendario().equals(existente.getCalendario().getOidcalendario())) {
                throw new NecesidadValidationException("El calendario no puede modificarse en una necesidad.");
            }

            Materia materiaDestino = existente.getMateria();
            if (request.getIdMateria() != null && !request.getIdMateria().equals(existente.getMateria().getIdMateria())) {
                materiaDestino = validarMateria(request.getIdMateria());
            }

            String grupoDestino = request.getGrupo() != null
                    ? normalizarGrupo(request.getGrupo())
                    : normalizarGrupo(existente.getGrupo());

            Optional<Necesidad> conflicto = necesidadRepository
                    .findByCalendario_OidcalendarioAndMateria_IdMateriaAndGrupo(
                            existente.getCalendario().getOidcalendario(),
                            materiaDestino.getIdMateria(),
                            grupoDestino);

            if (conflicto.isPresent() && !conflicto.get().getOidNecesidad().equals(existente.getOidNecesidad())) {
                throw new NecesidadValidationException(String.format(
                        "Ya existe una necesidad registrada para la materia %s en el grupo %s.",
                        materiaDestino.getNombre(), grupoDestino));
            }

            if (!materiaDestino.getIdMateria().equals(existente.getMateria().getIdMateria())) {
                existente.setMateria(materiaDestino);
            }

            necesidadMapper.actualizarCampos(existente, request);
            if (request.getGrupo() != null) {
                existente.setGrupo(grupoDestino);
            }

            if (request.getCorrequisitoOidNecesidad() != null) {
                Optional<Necesidad> correquisito = obtenerCorrequisito(existente.getCalendario().getOidcalendario(), request.getCorrequisitoOidNecesidad());
                existente.setCorrequisitoNecesidad(correquisito.orElse(null));
            }

            existente.setUsuarioActualizacion("system");
            Necesidad actualizada = necesidadRepository.save(existente);
            logger.info("Necesidad actualizada con ID: {}", oid);

            return new ApiResponse<>(200, "Necesidad actualizada correctamente.", necesidadMapper.toResponse(actualizada));
        } catch (NecesidadException e) {
            logger.warn("Error al actualizar necesidad: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
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
                throw new NecesidadNotFoundException("Necesidad no encontrada con ID: " + oid);
            }

            necesidadRepository.deleteById(oid);
            logger.info("Necesidad eliminada con ID: {}", oid);
            return new ApiResponse<>(204, "Necesidad eliminada correctamente.", null);
        } catch (NecesidadNotFoundException e) {
            logger.warn("Intento de eliminar necesidad inexistente: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error al eliminar necesidad", e);
            return new ApiResponse<>(500, "Error al eliminar la necesidad: " + e.getMessage(), null);
        }
    }

    private String normalizarGrupo(String grupo) {
        return (grupo != null) ? grupo.trim().toUpperCase() : null;
    }

    private String generarNombreGrupo(int indice) {
        int value = indice;
        StringBuilder builder = new StringBuilder();
        do {
            int remainder = value % 26;
            builder.insert(0, (char) ('A' + remainder));
            value = (value / 26) - 1;
        } while (value >= 0);
        return builder.toString();
    }

    private void validarParametrosListado(Integer oidCalendario, Integer oidPrograma) {
        if (oidCalendario == null) {
            throw new NecesidadCalendarioObligatorioException();
        }
        if (oidPrograma == null) {
            throw new NecesidadProgramaObligatorioException();
        }
    }

    private Calendario validarCalendario(Integer oidCalendario) {
        if (oidCalendario == null) {
            throw new NecesidadValidationException("El calendario es obligatorio.");
        }
        return calendarioRepository.findById(oidCalendario)
                .orElseThrow(() -> new NecesidadNotFoundException("Calendario no encontrado con ID: " + oidCalendario));
    }

    private Materia validarMateria(Integer idMateria) {
        if (idMateria == null) {
            throw new NecesidadValidationException("La materia es obligatoria.");
        }
        return materiaRepository.findById(idMateria)
                .orElseThrow(() -> new NecesidadNotFoundException("Materia no encontrada con ID: " + idMateria));
    }

    private Optional<Necesidad> obtenerCorrequisito(Integer oidCalendario, Integer correquisitoOid) {
        if (correquisitoOid == null) {
            return Optional.empty();
        }
        if (correquisitoOid <= 0) {
            return Optional.empty();
        }

        Necesidad correquisito = necesidadRepository.findById(correquisitoOid)
                .orElseThrow(() -> new NecesidadNotFoundException("Correquisito no encontrado con ID: " + correquisitoOid));

        if (!correquisito.getCalendario().getOidcalendario().equals(oidCalendario)) {
            throw new NecesidadValidationException("El correquisito debe pertenecer al mismo calendario.");
        }
        return Optional.of(correquisito);
    }

}
