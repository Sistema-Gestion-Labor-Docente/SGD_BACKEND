package co.edu.unicauca.sgd.api.service.necesidad.impl;

import java.util.Optional;

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
            return new ApiResponse<>(200, "Necesidades recuperadas correctamente.", response);
        } catch (Exception e) {
            logger.error("Error al listar necesidades", e);
            return new ApiResponse<>(500, "Error al listar las necesidades: " + e.getMessage(), Page.empty(pageableToUse));
        }
    }

    @Override
    public ApiResponse<NecesidadDTOResponse> buscarPorId(Integer oid) {
        try {
            Necesidad necesidad = necesidadRepository.findById(oid)
                    .orElseThrow(() -> new RuntimeException("Necesidad no encontrada con ID: " + oid));

            return new ApiResponse<>(200, "Necesidad encontrada correctamente.", necesidadMapper.toResponse(necesidad));
        } catch (RuntimeException e) {
            logger.warn("Necesidad no encontrada: {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error al buscar necesidad", e);
            return new ApiResponse<>(500, "Error interno: " + e.getMessage(), null);
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
                return new ApiResponse<>(404, "Necesidad no encontrada con ID: " + oid, null);
            }

            necesidadRepository.deleteById(oid);
            logger.info("Necesidad eliminada con ID: {}", oid);
            return new ApiResponse<>(204, "Necesidad eliminada correctamente.", null);
        } catch (Exception e) {
            logger.error("Error al eliminar necesidad", e);
            return new ApiResponse<>(500, "Error al eliminar la necesidad: " + e.getMessage(), null);
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
}
