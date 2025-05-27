package co.edu.unicauca.sed.api.service.proceso;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.PeriodoAcademico;
import co.edu.unicauca.sed.api.domain.Proceso;
import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.repository.ProcesoRepository;
import co.edu.unicauca.sed.api.service.periodo_academico.PeriodoAcademicoService;
import co.edu.unicauca.sed.api.specification.ProcesoSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProcesoServiceImpl implements ProcesoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcesoServiceImpl.class);
    private final ProcesoRepository procesoRepository;
    private final PeriodoAcademicoService periodoAcademicoService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<Proceso>> buscarTodos(Integer evaluadorId, Integer evaluadoId, Integer idPeriodo,
                                                  String nombreProceso, LocalDateTime fechaCreacion,
                                                  LocalDateTime fechaActualizacion, Pageable pageable) {
        try {
            if (idPeriodo == null) {
                idPeriodo = periodoAcademicoService.obtenerIdPeriodoAcademicoActivo();
            }

            Specification<Proceso> spec = ProcesoSpecification.byFilters(
                evaluadorId, evaluadoId, idPeriodo, nombreProceso, fechaCreacion, fechaActualizacion);

            Page<Proceso> procesos = procesoRepository.findAll(spec, pageable);

            return procesos.isEmpty()
                ? new ApiResponse<>(204, "No se encontraron procesos con los filtros dados.", Page.empty())
                : new ApiResponse<>(200, "Procesos obtenidos correctamente.", procesos);

        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener los procesos: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al obtener los procesos.", Page.empty());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Proceso> buscarPorId(Integer oid) {
        return procesoRepository.findById(oid)
            .map(proceso -> new ApiResponse<>(200, "Proceso encontrado correctamente.", proceso))
            .orElse(new ApiResponse<>(404, "Proceso con ID " + oid + " no encontrado.", null));
    }

    @Override
    @Transactional
    public ApiResponse<Proceso> guardar(Proceso proceso) {
        try {
            proceso.setNombreProceso(proceso.getNombreProceso().toUpperCase());
            Proceso savedProceso = procesoRepository.save(proceso);
            return new ApiResponse<>(201, "Proceso guardado correctamente.", savedProceso);
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("❌ Restricción de clave única violada al guardar el proceso: {}", e.getMessage());
            return new ApiResponse<>(409, "Error: Ya existe un proceso con estos datos.", null);
        } catch (Exception e) {
            LOGGER.error("❌ Error inesperado al guardar el proceso: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al guardar el proceso.", null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Proceso> actualizar(Integer oid, Proceso proceso) {
        try {
            Proceso existingProceso = procesoRepository.findById(oid).orElse(null);

            if (existingProceso == null) {
                return new ApiResponse<>(404, "Proceso con ID " + oid + " no encontrado.", null);
            }

            existingProceso.setEvaluador(proceso.getEvaluador());
            existingProceso.setEvaluado(proceso.getEvaluado());
            existingProceso.setOidPeriodoAcademico(proceso.getOidPeriodoAcademico());
            existingProceso.setNombreProceso(proceso.getNombreProceso().toUpperCase());
            existingProceso.setResolucion(proceso.getResolucion());

            Proceso updatedProceso = procesoRepository.save(existingProceso);
            LOGGER.info("✅ [UPDATE] Proceso actualizado con ID: {}", updatedProceso.getOidProceso());

            return new ApiResponse<>(200, "Proceso actualizado correctamente.", updatedProceso);
        } catch (Exception e) {
            LOGGER.error("❌ [ERROR] Error inesperado al actualizar el proceso: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al actualizar el proceso.", null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        if (!procesoRepository.existsById(oid)) {
            return new ApiResponse<>(404, "Proceso con ID " + oid + " no encontrado.", null);
        }
        procesoRepository.deleteById(oid);
        LOGGER.info("✅ Proceso eliminado con ID: {}", oid);
        return new ApiResponse<>(200, "Proceso eliminado correctamente.", null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> guardarProceso(Actividad actividad) {
        if (actividad.getProceso() != null) {
            try {
                Proceso savedProceso = procesoRepository.save(actividad.getProceso());
                actividad.setProceso(savedProceso);
                return new ApiResponse<>(200, "Proceso guardado correctamente.", null);
            } catch (DataIntegrityViolationException e) {
                LOGGER.error("❌ [ERROR] Restricción de clave única violada al guardar el proceso: {}", e.getMessage());
                return new ApiResponse<>(409, "Error: Ya existe un proceso con estos datos.", null);
            } catch (Exception e) {
                LOGGER.error("❌ [ERROR] Error inesperado al guardar el proceso: {}", e.getMessage(), e);
                return new ApiResponse<>(500, "Error inesperado al guardar el proceso.", null);
            }
        }
        return new ApiResponse<>(400, "Error: La actividad no contiene un proceso válido.", null);
    }

    @Override
    public Proceso buscarProcesoExistente(Integer idEvaluador, Integer idEvaluado, Integer idPeriodoAcademico, String nombreProceso) {
        try {
            return procesoRepository.findByEvaluadorAndEvaluadoAndOidPeriodoAcademicoAndNombreProceso(
                new Usuario(idEvaluador),
                new Usuario(idEvaluado),
                new PeriodoAcademico(idPeriodoAcademico),
                nombreProceso).orElse(null);
        } catch (Exception e) {
            LOGGER.error("❌ [ERROR] Error al buscar proceso existente: {}", e.getMessage(), e);
            return null; // Devolvemos `null` en caso de error para evitar interrupciones
        }
    }

    @Override
    @Transactional
    public Proceso crearNuevoProceso(Integer idEvaluador, Integer idEvaluado, Integer idPeriodoAcademico) {
        try {
            Proceso proceso = new Proceso();
            proceso.setEvaluador(new Usuario(idEvaluador));
            proceso.setEvaluado(new Usuario(idEvaluado));
            proceso.setOidPeriodoAcademico(new PeriodoAcademico(idPeriodoAcademico));
            proceso.setNombreProceso(TIPO_CONSOLIDADO);
    
            Proceso savedProceso = this.procesoRepository.save(proceso);    
            return savedProceso;
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("❌ [ERROR] Restricción de clave única violada al crear el proceso: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.error("❌ [ERROR] Error inesperado al crear el proceso: {}", e.getMessage(), e);
            return null;
        }
    }
}
