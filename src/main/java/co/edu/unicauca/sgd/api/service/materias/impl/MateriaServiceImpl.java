package co.edu.unicauca.sgd.api.service.materias.impl;

import co.edu.unicauca.sgd.api.domain.*;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTOResponse;
import co.edu.unicauca.sgd.api.mapper.MateriaMapper;
import co.edu.unicauca.sgd.api.repository.MateriaRepository;
import co.edu.unicauca.sgd.api.repository.DepartamentoRepository;
import co.edu.unicauca.sgd.api.repository.PlanRepository;
import co.edu.unicauca.sgd.api.service.materias.MateriaService;
import jakarta.transaction.Transactional;
import org.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MateriaServiceImpl implements MateriaService {

    private static final Logger logger = LoggerFactory.getLogger(MateriaServiceImpl.class);

    private MateriaRepository materiaRepository;
    
    private DepartamentoRepository departamentoRepository;
    
    private PlanRepository planRepository;
    
    private MateriaMapper materiaMapper;

    public MateriaServiceImpl(
            MateriaRepository materiaRepository,
            DepartamentoRepository departamentoRepository,
            PlanRepository planRepository,
            MateriaMapper materiaMapper) {
        this.materiaRepository = materiaRepository;
        this.departamentoRepository = departamentoRepository;
        this.planRepository = planRepository;
        this.materiaMapper = materiaMapper;
    }

    @Override
    public ApiResponse<Page<MateriaDTOResponse>> obtenerTodos(
            String oidmateria, String codigo, String nombre,
            Integer semestre, Integer oidDepartamento, Integer oidPlan,
            Pageable pageable) {

        try {
            Specification<Materia> spec = Specification.where(null);

            if (StringUtils.hasText(oidmateria)) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(cb.upper(root.get("oidMateria")), oidmateria.toUpperCase()));
            }
            if (StringUtils.hasText(codigo)) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.upper(root.get("codigo")), "%" + codigo.toUpperCase() + "%"));
            }
            if (StringUtils.hasText(nombre)) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.upper(root.get("nombre")), "%" + nombre.toUpperCase() + "%"));
            }
            if (semestre != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("semestre"), semestre));
            }
            if (oidDepartamento != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.join("departamento").get("oidDepartamento"), oidDepartamento));
            }
            if (oidPlan != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.join("plan").get("oidPlan"), oidPlan));
            }

            Page<Materia> page = materiaRepository.findAll(spec, pageable);
            Page<MateriaDTOResponse> response = page.map(materiaMapper::toResponse);

            logger.info("Materias encontradas: {}", response.getTotalElements());
            return new ApiResponse<>(200, "Materias recuperadas correctamente.", response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al listar materias: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<MateriaDTOResponse> buscarPorId(Integer id) {
        try {
            Materia entity = materiaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + id));
            return new ApiResponse<>(200, "Materia encontrada correctamente.", materiaMapper.toResponse(entity));
        } catch (RuntimeException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<MateriaDTOResponse> guardar(MateriaDTORequest request) {
        try {
            Materia entity = materiaMapper.convertToEntity(request);

            if (request.getOidDepartamento() != null && request.getOidDepartamento() > 0) {
                Departamento dep = departamentoRepository.findById(request.getOidDepartamento())
                    .orElseThrow(() -> new RuntimeException("Departamento no encontrado con ID: " + request.getOidDepartamento()));
                entity.setDepartamento(dep);
            }
            Plan plan = planRepository.findById(request.getOidPlan())
                    .orElseThrow(() -> new RuntimeException("Plan no encontrado con ID: " + request.getOidPlan()));

            Materia correquisito = resolverCorrequisito(request, null);

            entity.setPlan(plan);
            entity.setCorrequisito(correquisito);

            entity.setUsuarioCreacion("Usuario");
            Materia saved = materiaRepository.save(entity);

            logger.info("Materia guardada ID: {}", saved.getOidMateria());
            return new ApiResponse<>(200, "Materia guardada correctamente.", materiaMapper.toResponse(saved));
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, "Error en datos: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar la materia: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<MateriaDTOResponse> actualizar(Integer id, MateriaDTORequest request) {
        try {
            Materia existente = materiaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + id));

            // actualizar básicos
            materiaMapper.actualizarCamposBasicos(existente, request);

            // actualizar relaciones si vienen
            if (request.getOidDepartamento() != null) {
                Departamento dep = departamentoRepository.findById(request.getOidDepartamento())
                        .orElseThrow(() -> new RuntimeException("Departamento no encontrado con ID: " + request.getOidDepartamento()));
                existente.setDepartamento(dep);
            }
            if (request.getOidPlan() != null) {
                Plan plan = planRepository.findById(request.getOidPlan())
                        .orElseThrow(() -> new RuntimeException("Plan no encontrado con ID: " + request.getOidPlan()));
                existente.setPlan(plan);
            }
            if (request.getIdCorrequisito() != null) {
                Materia correquisito = resolverCorrequisito(request, existente.getIdMateria());
                existente.setCorrequisito(correquisito);
            }

            existente.setUsuarioActualizacion("UsuarioActualizacion");
            Materia actualizado = materiaRepository.save(existente);

            logger.info("Materia actualizada ID: {}", id);
            return new ApiResponse<>(200, "Materia actualizada correctamente.", materiaMapper.toResponse(actualizado));
        } catch (RuntimeException e) {
            return new ApiResponse<>(400, "Error en la actualización: " + e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al actualizar la materia: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer id) {
        try {
            if (!materiaRepository.existsById(id)) {
                return new ApiResponse<>(404, "Materia no encontrada con ID: " + id, null);
            }
            materiaRepository.deleteById(id);
            logger.info("Materia eliminada ID: {}", id);
            return new ApiResponse<>(200, "Materia eliminada correctamente.", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar la materia: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Page<MateriaDTOResponse>> obtenerMateriasSinCorrequisitoNiReferencias(
            Integer oidDepartamento,
            Integer oidPlan,
            Pageable pageable) {
        try {
            if (oidPlan == null) {
                return new ApiResponse<>(400, "Se requiere el plan para realizar la consulta.", null);
            }
            Page<Materia> materias = materiaRepository.findMateriasSinCorrequisitoNiReferencias(
                    oidDepartamento,
                    oidPlan,
                    pageable);
            Page<MateriaDTOResponse> response = materias.map(materiaMapper::toResponse);

            logger.info("Materias sin correquisito encontradas: {}", response.getTotalElements());
            return new ApiResponse<>(200, "Materias libres sin correquisito recuperadas correctamente.", response);
        } catch (Exception e) {
            logger.error("Error al obtener materias sin correquisito: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error al obtener materias sin correquisito: " + e.getMessage(), null);
        }
    }

    private Materia resolverCorrequisito(MateriaDTORequest request, Integer idMateriaActual) {
        Integer idCorrequisito = request.getIdCorrequisito();

        if (idCorrequisito == null) {
            return null;
        }

        Materia correquisito = materiaRepository.findById(idCorrequisito)
                .orElseThrow(() -> new RuntimeException("Correquisito no encontrado con ID: " + idCorrequisito));

        if (idMateriaActual != null && correquisito.getIdMateria() != null
                && correquisito.getIdMateria().equals(idMateriaActual)) {
            throw new RuntimeException("La materia no puede ser correquisito de sí misma.");
        }
        if (StringUtils.hasText(request.getOidMateria())
                && correquisito.getOidMateria() != null
                && correquisito.getOidMateria().equalsIgnoreCase(request.getOidMateria())) {
            throw new RuntimeException("La materia no puede ser correquisito de sí misma.");
        }

        validarDisponibilidadCorrequisito(correquisito, idMateriaActual);
        return correquisito;
    }

    private void validarDisponibilidadCorrequisito(Materia correquisito, Integer idMateriaActual) {
        if (correquisito.getCorrequisito() != null) {
            if (idMateriaActual == null || !correquisito.getCorrequisito().getIdMateria().equals(idMateriaActual)) {
                throw new RuntimeException("La materia seleccionada como correquisito ya tiene un correquisito asignado.");
            }
        }

        boolean asociadaAOtraMateria = (idMateriaActual == null)
                ? materiaRepository.existsByCorrequisito(correquisito)
                : materiaRepository.existsByCorrequisitoAndIdMateriaNot(correquisito, idMateriaActual);
        if (asociadaAOtraMateria) {
            throw new RuntimeException("La materia seleccionada como correquisito ya está relacionada con otra materia.");
        }
    }
}
