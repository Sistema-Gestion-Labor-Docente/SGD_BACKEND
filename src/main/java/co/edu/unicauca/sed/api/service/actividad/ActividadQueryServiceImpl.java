package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.domain.Proceso;
import co.edu.unicauca.sed.api.domain.Rol;
import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sed.api.dto.actividad.ActividadDTOEvaluador;
import co.edu.unicauca.sed.api.repository.ActividadRepository;
import co.edu.unicauca.sed.api.service.periodo_academico.PeriodoAcademicoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para consultas avanzadas sobre actividades.
 */
@Service
public class ActividadQueryServiceImpl implements ActividadQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ActividadQueryServiceImpl.class);
    public static final boolean DEFAULT_ASCENDING_ORDER = true;

    @PersistenceContext
    private EntityManager entityManager;

    private final ActividadDTOService actividadDTOService;
    private final PeriodoAcademicoService periodoAcademicoService;
    private final ActividadRepository actividadRepository;

    public ActividadQueryServiceImpl(ActividadDTOService actividadDTOService,
            PeriodoAcademicoService periodoAcademicoService,
            ActividadRepository actividadRepository) {
        this.actividadDTOService = actividadDTOService;
        this.periodoAcademicoService = periodoAcademicoService;
        this.actividadRepository = actividadRepository;
    }

    @Override
    public ApiResponse<Page<ActividadBaseDTO>> buscarActividadesPorEvaluado(
            Integer evaluatorUserId, Integer evaluatedUserId, String activityCode, String activityType,
            String evaluatorName, List<String> roles, String sourceType, String sourceStatus,
            Boolean ascendingOrder, Integer idPeriodoAcademico, Boolean asignacionDefault, Pageable pageable) {

        Specification<Actividad> spec = filtrarActividades(evaluatorUserId, evaluatedUserId, activityCode,
                activityType, evaluatorName, roles, sourceType, sourceStatus, ascendingOrder, idPeriodoAcademico, asignacionDefault);

        Page<Actividad> activitiesPage = actividadRepository.findAll(spec, pageable);
        List<ActividadBaseDTO> activityDTOs = activitiesPage.getContent().stream()
            .map(actividadDTOService::buildActividadBaseDTO).collect(Collectors.toList());

        return new ApiResponse<>(200, "Actividades obtenidas correctamente.",
            new PageImpl<>(activityDTOs, pageable, activitiesPage.getTotalElements()));
    }

    @Override
    public ApiResponse<Page<ActividadDTOEvaluador>> buscarActividadesPorEvaluador(
        Integer evaluatorUserId, Integer evaluatedUserId, String activityCode, String activityType,
        String evaluatorName, List<String> roles, String sourceType, String sourceStatus,
        Boolean ascendingOrder, Integer idPeriodoAcademico, Boolean asignacionDefault, Pageable pageable) {

        Specification<Actividad> spec = filtrarActividades(evaluatorUserId, evaluatedUserId, activityCode, activityType, evaluatorName, 
        roles, sourceType, sourceStatus, ascendingOrder, idPeriodoAcademico, asignacionDefault);

        Page<Actividad> activitiesPage = actividadRepository.findAll(spec, pageable);
        List<ActividadDTOEvaluador> activityDTOs = activitiesPage.getContent().stream()
                .map(activity -> (sourceType != null || sourceStatus != null)
                        ? actividadDTOService.convertToDTOWithEvaluado(activity, sourceType, sourceStatus)
                        : actividadDTOService.convertToDTOWithEvaluado(activity)).collect(Collectors.toList());

        return new ApiResponse<>(200, "Actividades obtenidas correctamente.",
                new PageImpl<>(activityDTOs, pageable, activitiesPage.getTotalElements()));
    }

    @Override
    public Page<Actividad> obtenerActividadesPorProcesosPaginadas(List<Proceso> procesos, Pageable pageable) {
        List<Integer> procesoIds = procesos.stream().map(Proceso::getOidProceso).collect(Collectors.toList());
        return actividadRepository.findByProcesos(procesoIds, pageable);
    }

    @Override
    public Specification<Actividad> filtrarActividades(
            Integer userEvaluatorId, Integer userEvaluatedId, String activityCode, String activityType,
            String evaluatorName, List<String> roles, String sourceType, String sourceStatus,
            Boolean ascendingOrder, Integer idPeriodoAcademico, Boolean asignacionDefault) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Integer finalIdPeriodoAcademico;
            try {
                finalIdPeriodoAcademico = (idPeriodoAcademico != null) ? idPeriodoAcademico : periodoAcademicoService.obtenerIdPeriodoAcademicoActivo();
            } catch (IllegalStateException e) {
                logger.warn("⚠️ [PERIODO] No se encontró un período académico activo antes de ejecutar la consulta.");
                throw new EntityNotFoundException("No se encontró un período académico activo.");
            }
            predicates.add(cb.equal(root.get("proceso").get("oidPeriodoAcademico").get("oidPeriodoAcademico"), finalIdPeriodoAcademico));

            if (userEvaluatorId != null) {
                predicates.add(cb.equal(root.join("proceso").join("evaluador").get("oidUsuario"), userEvaluatorId));

                if (roles != null && !roles.isEmpty()) {
                    Join<Usuario, Rol> joinRoles = root.join("proceso").join("evaluado").join("roles");
                    predicates.add(joinRoles.get("oid").in(roles));
                }

                if (evaluatorName != null && !evaluatorName.isEmpty()) {
                    Expression<String> nombreCompleto = cb.concat(
                        cb.concat(cb.upper(root.join("proceso").join("evaluado").get("nombres"))," "),
                        cb.upper(root.join("proceso").join("evaluado").get("apellidos")));
                    predicates.add(cb.like(nombreCompleto, "%" + evaluatorName.toUpperCase() + "%"));
                }
            }

            if (userEvaluatedId != null) {
                predicates.add(cb.equal(root.join("proceso").join("evaluado").get("oidUsuario"), userEvaluatedId));

                if (evaluatorName != null && !evaluatorName.isEmpty()) {
                    Expression<String> nombreCompleto = cb.concat(
                        cb.concat(cb.upper(root.join("proceso").join("evaluador").get("nombres"))," "),
                        cb.upper(root.join("proceso").join("evaluador").get("apellidos")));
                    predicates.add(cb.like(nombreCompleto, "%" + evaluatorName.toUpperCase() + "%"));
                }
    
                if (roles != null && !roles.isEmpty()) {
                    Join<Usuario, Rol> joinRoles = root.join("proceso").join("evaluador").join("roles");
                    predicates.add(joinRoles.get("oid").in(roles));
                }
            }

            if (asignacionDefault != null) {
                predicates.add(cb.equal(root.get("asignacionDefault"), asignacionDefault));
            }

            if (activityCode != null && !activityCode.isEmpty()) {
                predicates.add(cb.like(root.get("nombreActividad"), "%" + activityCode + "%"));
            }

            if (activityType != null && !activityType.isEmpty()) {
                predicates.add(cb.equal(root.join("tipoActividad").get("oidTipoActividad"), Integer.parseInt(activityType)));
            }

            query.distinct(true);

            aplicarOrdenacion(query, cb, root, ascendingOrder, sourceType, sourceStatus);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void aplicarOrdenacion(CriteriaQuery<?> query, CriteriaBuilder cb, Root<Actividad> root,
            Boolean ascendingOrder, String sourceType, String sourceStatus) {
        boolean isAscending = (ascendingOrder != null) ? ascendingOrder : DEFAULT_ASCENDING_ORDER;
        List<Order> orderList = new ArrayList<>();

        boolean ordenarPorFuente = (sourceType != null && !sourceType.isEmpty()) || (sourceStatus != null && !sourceStatus.isEmpty());

        if (ordenarPorFuente) {
            Join<Actividad, Fuente> fuenteJoin = root.join("fuentes", JoinType.LEFT);

            if (sourceType != null && !sourceType.isEmpty()) {
                orderList.add(
                        isAscending ? cb.asc(fuenteJoin.get("tipoFuente")) : cb.desc(fuenteJoin.get("tipoFuente")));
            }

            if (sourceStatus != null && !sourceStatus.isEmpty()) {
                orderList.add(isAscending ? cb.asc(fuenteJoin.get("estadoFuente").get("oidEstadoFuente"))
                        : cb.desc(fuenteJoin.get("estadoFuente").get("oidEstadoFuente")));
            }
        } else {
            orderList.add(isAscending ? cb.asc(root.get("nombreActividad")) : cb.desc(root.get("nombreActividad")));
        }

        if (!orderList.isEmpty()) {
            query.orderBy(orderList);
        }
    }

    public List<ActividadBaseDTO> ordenarActividadesPorTipo(List<ActividadBaseDTO> actividades, Boolean ordenAscendente) {
        // Crear un Comparator explícito para ActividadBaseDTO
        Comparator<ActividadBaseDTO> comparador = Comparator.comparing(actividad -> actividad.getTipoActividad().getNombre());

        // Invertir el orden si no es ascendente
        if (ordenAscendente != null && !ordenAscendente) {
            comparador = comparador.reversed();
        }

        // Ordenar y retornar la lista de actividades
        return actividades.stream().sorted(comparador).collect(Collectors.toList());
    }
}
