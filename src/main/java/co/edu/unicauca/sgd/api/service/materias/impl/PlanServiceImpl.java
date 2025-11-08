package co.edu.unicauca.sgd.api.service.materias.impl;

import co.edu.unicauca.sgd.api.domain.Plan;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTOResponse;
import co.edu.unicauca.sgd.api.mapper.PlanMapper;
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

    private PlanMapper planMapper;

    public PlanServiceImpl(PlanRepository planRepository, PlanMapper planMapper) {
        this.planRepository = planRepository;
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
            Page<PlanDTOResponse> response = page.map(planMapper::toResponse);

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
                    .orElseThrow(() -> new IllegalStateException("Plan no encontrado con ID: " + oid));
            return new ApiResponse<>(200, "Plan encontrado correctamente.", planMapper.toResponse(entity));
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al buscar el plan: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<PlanDTOResponse> guardar(PlanDTORequest request) {
        try {
            Plan entity = planMapper.convertToEntity(request);
            entity.setUsuarioCreacion("Usuario"); // igual que Calendario
            Plan saved = planRepository.save(entity);
            logger.info("Plan guardado ID: {}", saved.getOidPlan());
            return new ApiResponse<>(200, "Plan guardado correctamente.", planMapper.toResponse(saved));
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al guardar el plan: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<PlanDTOResponse> actualizar(Integer oid, PlanDTORequest request) {
        try {
            Plan existente = planRepository.findById(oid)
                    .orElseThrow(() -> new IllegalStateException("Plan no encontrado con ID: " + oid));
            planMapper.actualizarCamposBasicos(existente, request);
            existente.setUsuarioActualizacion("UsuarioActualizacion");
            Plan actualizado = planRepository.save(existente);
            logger.info("Plan actualizado ID: {}", oid);
            return new ApiResponse<>(200, "Plan actualizado correctamente.", planMapper.toResponse(actualizado));
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error interno al actualizar el plan: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            if (!planRepository.existsById(oid)) {
                throw new IllegalStateException("Plan no encontrado con ID: " + oid);
            }
            planRepository.deleteById(oid);
            logger.info("Plan eliminado ID: {}", oid);
            return new ApiResponse<>(200, "Plan eliminado correctamente.", null);
        } catch (IllegalStateException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al eliminar el plan: " + e.getMessage(), null);
        }
    }
}
