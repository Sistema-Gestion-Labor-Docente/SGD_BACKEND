package co.edu.unicauca.sed.api.service.periodo_academico;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import co.edu.unicauca.sed.api.client.ClientePeriodoAcademico;
import co.edu.unicauca.sed.api.domain.EstadoPeriodoAcademico;
import co.edu.unicauca.sed.api.domain.PeriodoAcademico;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.PeriodoExternoDTO;
import co.edu.unicauca.sed.api.repository.EstadoPeriodoAcademicoRepository;
import co.edu.unicauca.sed.api.repository.PeriodoAcademicoRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class PeriodoAcademicoServiceImpl implements PeriodoAcademicoService {

    @Autowired
    private PeriodoAcademicoRepository periodoAcademicoRepository;

    @Autowired
    private EstadoPeriodoAcademicoRepository estadoPeriodoAcademicoRepository;

    @Autowired
    private ClientePeriodoAcademico clientePeriodoAcademico;

    private static final Logger logger = LoggerFactory.getLogger(PeriodoAcademicoService.class);

    @Override
    public ApiResponse<Page<PeriodoAcademico>> obtenerTodos(Pageable pageable) {
        try {
            Page<PeriodoAcademico> periodos = periodoAcademicoRepository.findAll(pageable);
            if (periodos.isEmpty()) {
                return new ApiResponse<>(204, "No se encontraron períodos académicos.", Page.empty());
            }
            return new ApiResponse<>(200, "Períodos académicos obtenidos correctamente.", periodos);
        } catch (Exception e) {
            logger.error("❌ [ERROR] Error al obtener los períodos académicos: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al obtener los períodos académicos.", Page.empty());
        }
    }

    @Override
    public ApiResponse<PeriodoAcademico> buscarPorId(Integer oid) {
        try {
            PeriodoAcademico periodo = periodoAcademicoRepository.findById(oid)
                .orElseThrow(
                    () -> new EntityNotFoundException("Período académico con ID " + oid + " no encontrado."));
            return new ApiResponse<>(200, "Período académico encontrado correctamente.", periodo);
        } catch (EntityNotFoundException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("❌ [ERROR] Error al buscar el período académico: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al buscar el período académico.", null);
        }
    }

    @Override
    public ApiResponse<PeriodoAcademico> guardar(PeriodoAcademico periodoAcademico) {
        try {
            validatePeriodoAcademico(null, periodoAcademico);
            PeriodoAcademico saved = periodoAcademicoRepository.save(periodoAcademico);
            return new ApiResponse<>(201, "Período académico guardado correctamente.", saved);
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("❌ [ERROR] Error al guardar el período académico: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al guardar el período académico.", null);
        }
    }

    @Override
    public ApiResponse<Void> actualizar(Integer oid, PeriodoAcademico periodoAcademico) {
        try {
            Integer estadoId = periodoAcademico.getEstadoPeriodoAcademico().getOidEstadoPeriodoAcademico();
            EstadoPeriodoAcademico estado = estadoPeriodoAcademicoRepository.findById(estadoId)
                .orElseThrow(() -> new IllegalArgumentException("El EstadoPeriodoAcademico con OID " + estadoId + " no existe."));

            periodoAcademico.setEstadoPeriodoAcademico(estado);
            validatePeriodoAcademico(oid, periodoAcademico);

            periodoAcademicoRepository.findById(oid).orElseThrow(() -> new EntityNotFoundException("Período académico con ID " + oid + " no encontrado."));

            periodoAcademico.setOidPeriodoAcademico(oid);
            periodoAcademicoRepository.save(periodoAcademico);
            return new ApiResponse<>(200, "Período académico actualizado correctamente.", null);
        } catch (EntityNotFoundException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("❌ [ERROR] Error al actualizar el período académico: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al actualizar el período académico.", null);
        }
    }

    @Override
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            periodoAcademicoRepository.deleteById(oid);
            return new ApiResponse<>(200, "Período académico eliminado correctamente.", null);
        } catch (Exception e) {
            logger.error("❌ [ERROR] Error al eliminar el período académico: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Error inesperado al eliminar el período académico.", null);
        }
    }

    @Override
    public ApiResponse<List<PeriodoExternoDTO>> obtenerPeriodosNoRegistrados() {
        List<Integer> idsLocales = periodoAcademicoRepository.findAll().stream()
            .map(PeriodoAcademico::getIdPeriodoApi).filter(Objects::nonNull).toList();

        List<PeriodoExternoDTO> periodosExternos = clientePeriodoAcademico.obtenerPeriodosExternos();

        List<PeriodoExternoDTO> noRegistrados = periodosExternos.stream()
            .filter(pe -> !idsLocales.contains(pe.getId())).toList();

        return new ApiResponse<>(200, "Periodos no registrados obtenidos con éxito.", noRegistrados);
    }

    /**
     * Valida las condiciones necesarias para guardar o actualizar un período académico.
     *
     * @param oid              El identificador del período académico (puede ser null para un nuevo registro).
     * @param periodoAcademico El objeto PeriodoAcademico que se desea guardar o actualizar.
     * @throws IllegalArgumentException Si se violan las reglas de validación.
     */
    private void validatePeriodoAcademico(Integer oid, PeriodoAcademico periodoAcademico) {
        if (periodoAcademicoRepository.existsByIdPeriodo(periodoAcademico.getIdPeriodo())) {
            if (oid == null) {
                throw new IllegalArgumentException("El ID del período académico '" + periodoAcademico.getIdPeriodo() + "' ya existe.");
            }
    
            ApiResponse<PeriodoAcademico> response = buscarPorId(oid);
            PeriodoAcademico periodoExistente = response.getData();
    
            if (periodoExistente != null && !periodoExistente.getIdPeriodo().equals(periodoAcademico.getIdPeriodo())) {
                throw new IllegalArgumentException("El ID del período académico '" + periodoAcademico.getIdPeriodo() + "' ya existe en otro registro.");
            }
        }
    
        EstadoPeriodoAcademico estadoPeriodoAcademico = estadoPeriodoAcademicoRepository
            .findById(periodoAcademico.getEstadoPeriodoAcademico().getOidEstadoPeriodoAcademico())
            .orElseThrow(() -> new IllegalArgumentException("El EstadoPeriodoAcademico con OID "
                + periodoAcademico.getEstadoPeriodoAcademico().getOidEstadoPeriodoAcademico() + " no existe."));
    
        if ("ACTIVO".equals(estadoPeriodoAcademico.getNombre())) {
            ApiResponse<PeriodoAcademico> response = obtenerPeriodoAcademicoActivo();
        
            // Verificamos si hay un periodo activo (código 200 y data no nula)
            if (response.getCodigo() == 200 && response.getData() != null) {
                PeriodoAcademico periodoActivo = response.getData();
        
                // Validamos si ya existe un periodo activo diferente al que se está actualizando
                if (oid == null || !periodoActivo.getOidPeriodoAcademico().equals(oid)) {
                    throw new IllegalArgumentException(
                        "Ya existe un período académico activo con ID: " + periodoActivo.getIdPeriodo());
                }
            }
        }       
    }

    /**
     * Obtiene el período académico que está marcado como activo en la base de datos.
     *
     * @return Un Optional que contiene el período académico activo si existe.
     */
    @Override
    public ApiResponse<PeriodoAcademico> obtenerPeriodoAcademicoActivo() {
        String nombreEstadoActivo = "ACTIVO";

        return periodoAcademicoRepository.findByEstadoPeriodoAcademicoNombre(nombreEstadoActivo)
            .map(periodo -> {
                return new ApiResponse<>(200, "Período académico activo encontrado", periodo);
            })
            .orElseGet(() -> {
                logger.warn("⚠️ No se encontró un período académico activo.");
                return new ApiResponse<>(404, "No se encontró un período académico activo", null);
            });
    }

    /**
     * Obtiene el identificador del período académico activo.
     *
     * @return El identificador del período académico activo.
     * @throws IllegalStateException Si no se encuentra un período académico activo.
     */
    public Integer obtenerIdPeriodoAcademicoActivo() {
        ApiResponse<PeriodoAcademico> response = obtenerPeriodoAcademicoActivo();
    
        if (response.getCodigo() != 200 || response.getData() == null) {
            throw new EntityNotFoundException("No se encontró un período académico activo.");
        }
    
        return response.getData().getOidPeriodoAcademico();
    }
}
