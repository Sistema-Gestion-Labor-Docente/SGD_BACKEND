package co.edu.unicauca.sgd.api.service.materias.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTOResponse;
import co.edu.unicauca.sgd.api.exception.materias.MateriasException;
import co.edu.unicauca.sgd.api.exception.materias.PlanNotFoundException;
import co.edu.unicauca.sgd.api.mapper.PlanMapper;
import co.edu.unicauca.sgd.api.repository.MateriaRepository;
import co.edu.unicauca.sgd.api.repository.PlanRepository;
import co.edu.unicauca.sgd.api.service.materias.PlanService;
import jakarta.transaction.Transactional;
import org.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PlanServiceImpl implements PlanService {

    private static final Logger logger = LoggerFactory.getLogger(PlanServiceImpl.class);

    private PlanRepository planRepository;

    private MateriaRepository materiaRepository;

    private PlanMapper planMapper;

    public PlanServiceImpl(PlanRepository planRepository,
                           MateriaRepository materiaRepository,
                           PlanMapper planMapper) {
        this.planRepository = planRepository;
        this.materiaRepository = materiaRepository;
        this.planMapper = planMapper;
    }

    @Override
    public ApiResponse<Page<PlanDTOResponse>> obtenerTodos(String numero, String estado, Integer oidPrograma, Pageable pageable) {
        try {
            Specification<Plan> spec = Specification.where(null);

            if (StringUtils.hasText(numero)) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.upper(root.get("numero")), "%" + numero.toUpperCase() + "%"));
            }
            if (StringUtils.hasText(estado)) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(cb.upper(root.get("estado")), estado.toUpperCase()));
            }
            if (oidPrograma != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("programa").get("oidPrograma"), oidPrograma));
            }

            Page<Plan> page = planRepository.findAll(spec, pageable);
            Page<PlanDTOResponse> response = page.map(plan -> {
                PlanDTOResponse dto = planMapper.toResponse(plan);
                long cantidadMaterias = materiaRepository.countByPlanOidPlan(plan.getOidPlan());
                dto.setCantidadMaterias(cantidadMaterias);
                return dto;
            });

            logger.info("Planes encontrados: {}", response.getTotalElements());
            String message = response.hasContent()
                    ? "Planes recuperados correctamente."
                    : "No se encontraron planes.";
            return new ApiResponse<>(200, message, response);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al listar planes: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<PlanDTOResponse> buscarPorId(Integer oid) {
        try {
            Plan entity = planRepository.findById(oid)
                    .orElseThrow(() -> new PlanNotFoundException("Plan no encontrado con ID: " + oid));
            PlanDTOResponse dto = planMapper.toResponse(entity);
            long cantidadMaterias = materiaRepository.countByPlanOidPlan(entity.getOidPlan());
            dto.setCantidadMaterias(cantidadMaterias);
            return new ApiResponse<>(200, "Plan encontrado correctamente.", dto);
        } catch (MateriasException e) {
            logger.warn("Error de negocio al buscar plan: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al buscar el plan: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<PlanDTOResponse> guardar(PlanDTORequest request) {
        try {
            Integer oidPlanBase = request.getOidPlanBase();
            List<Materia> materiasBase = new ArrayList<>();

            if (oidPlanBase != null) {
                planRepository.findById(oidPlanBase)
                        .orElseThrow(() -> new PlanNotFoundException("Plan base no encontrado con ID: " + oidPlanBase));
                materiasBase = materiaRepository.findAllByPlanOidPlan(oidPlanBase);
            }

            Plan entity = planMapper.convertToEntity(request);
            entity.setUsuarioCreacion("Usuario"); // igual que Calendario
            Plan saved = planRepository.save(entity);

            if (!materiasBase.isEmpty()) {
                clonarMateriasDesdePlanBase(materiasBase, saved);
            }

            long cantidadMaterias = materiaRepository.countByPlanOidPlan(saved.getOidPlan());
            PlanDTOResponse dto = planMapper.toResponse(saved);
            dto.setCantidadMaterias(cantidadMaterias);

            logger.info("Plan guardado ID: {}", saved.getOidPlan());
            return new ApiResponse<>(200, "Plan guardado correctamente.", dto);
        } catch (MateriasException e) {
            logger.warn("Error de negocio al guardar plan: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar el plan: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<PlanDTOResponse> actualizar(Integer oid, PlanDTORequest request) {
        try {
            Plan existente = planRepository.findById(oid)
                    .orElseThrow(() -> new PlanNotFoundException("Plan no encontrado con ID: " + oid));
            planMapper.actualizarCamposBasicos(existente, request);
            existente.setUsuarioActualizacion("UsuarioActualizacion");
            Plan actualizado = planRepository.save(existente);

            long cantidadMaterias = materiaRepository.countByPlanOidPlan(actualizado.getOidPlan());
            PlanDTOResponse dto = planMapper.toResponse(actualizado);
            dto.setCantidadMaterias(cantidadMaterias);

            logger.info("Plan actualizado ID: {}", oid);
            return new ApiResponse<>(200, "Plan actualizado correctamente.", dto);
        } catch (MateriasException e) {
            logger.warn("Error de negocio al actualizar plan: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al actualizar el plan: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!planRepository.existsById(oid)) {
                throw new PlanNotFoundException("Plan no encontrado con ID: " + oid);
            }
            planRepository.deleteById(oid);
            logger.info("Plan eliminado ID: {}", oid);
            return new ApiResponse<>(200, "Plan eliminado correctamente.", null);
        } catch (MateriasException e) {
            logger.warn("Error de negocio al eliminar plan: {}", e.getMessage());
            return new ApiResponse<>(e.getStatus().value(), e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar el plan: " + e.getMessage(), null);
        }
    }

    /**
     * Clona la lista de materias de un plan base hacia un nuevo plan,
     * preservando las relaciones de correquisito entre las materias clonadas.
     */
    private void clonarMateriasDesdePlanBase(List<Materia> materiasBase, Plan nuevoPlan) {
        Map<Integer, Materia> mapaOriginalANuevo = new HashMap<>();
        List<Materia> nuevasMaterias = new ArrayList<>();

        for (Materia base : materiasBase) {
            Materia nueva = new Materia();
            nueva.setOidMateria(base.getOidMateria());
            nueva.setCodigo(base.getCodigo());
            nueva.setNombre(base.getNombre());
            nueva.setSemestre(base.getSemestre());
            nueva.setHorasSemana(base.getHorasSemana());
            nueva.setDepartamento(base.getDepartamento());
            nueva.setPlan(nuevoPlan);
            nueva.setUsuarioCreacion("Usuario");

            nuevasMaterias.add(nueva);
            mapaOriginalANuevo.put(base.getIdMateria(), nueva);
        }

        materiaRepository.saveAll(nuevasMaterias);

        for (Materia base : materiasBase) {
            if (base.getCorrequisito() != null) {
                Materia nueva = mapaOriginalANuevo.get(base.getIdMateria());
                Materia nuevoCorrequisito = mapaOriginalANuevo.get(base.getCorrequisito().getIdMateria());
                if (nueva != null && nuevoCorrequisito != null) {
                    nueva.setCorrequisito(nuevoCorrequisito);
                }
            }
        }

        materiaRepository.saveAll(mapaOriginalANuevo.values());
    }
}
