package co.edu.unicauca.sgd.api.service.periodo_academico;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.sgd.api.domain.EstadoPeriodoAcademico;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.repository.EstadoPeriodoAcademicoRepository;

/**
 * Implementación del servicio para la gestión de estados de períodos académicos.
 */
@Service
@RequiredArgsConstructor
public class EstadoPeriodoAcademicoServicioImpl implements EstadoPeriodoAcademicoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EstadoPeriodoAcademicoServicioImpl.class);

    private final EstadoPeriodoAcademicoRepository repository;

    @Override
    @Transactional
    public ApiResponse<EstadoPeriodoAcademico> guardar(EstadoPeriodoAcademico estadoPeriodoAcademico) {
        try {
            EstadoPeriodoAcademico guardado = repository.save(estadoPeriodoAcademico);
            return new ApiResponse<>(201, "Estado de período académico guardado correctamente.", guardado);
        } catch (Exception e) {
            LOGGER.error("❌ Error al guardar EstadoPeriodoAcademico", e);
            return new ApiResponse<>(500, "Error inesperado al guardar el estado de período académico.", null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<EstadoPeriodoAcademico> buscarPorId(Integer id) {
        try {
            EstadoPeriodoAcademico estado = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("EstadoPeriodoAcademico con ID " + id + " no encontrado."));
            return new ApiResponse<>(200, "Estado de período académico encontrado correctamente.", estado);
        } catch (EntityNotFoundException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            LOGGER.error("❌ Error al buscar EstadoPeriodoAcademico con ID: {}", id, e);
            return new ApiResponse<>(500, "Error inesperado al buscar el estado de período académico.", null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<EstadoPeriodoAcademico>> buscarTodos(Pageable pageable) {
        try {
            Page<EstadoPeriodoAcademico> estados = repository.findAll(pageable);
            return estados.isEmpty()
                    ? new ApiResponse<>(204, "No se encontraron estados de períodos académicos.", Page.empty())
                    : new ApiResponse<>(200, "Estados de períodos académicos obtenidos correctamente.", estados);
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener EstadosPeriodoAcademico", e);
            return new ApiResponse<>(500, "Error inesperado al obtener los estados de períodos académicos.", Page.empty());
        }
    }

    @Override
    @Transactional
    public ApiResponse<EstadoPeriodoAcademico> actualizar(Integer id, EstadoPeriodoAcademico estadoPeriodoAcademico) {
        try {
            EstadoPeriodoAcademico estadoExistente = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("EstadoPeriodoAcademico con ID " + id + " no encontrado."));

            estadoExistente.setNombre(estadoPeriodoAcademico.getNombre());
            repository.save(estadoExistente);
            return new ApiResponse<>(200, "Estado de período académico actualizado correctamente.", estadoExistente);
        } catch (EntityNotFoundException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            LOGGER.error("❌ Error al actualizar EstadoPeriodoAcademico con ID: {}", id, e);
            return new ApiResponse<>(500, "Error inesperado al actualizar el estado de período académico.", null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer id) {
        try {
            LOGGER.info("🗑 Eliminando EstadoPeriodoAcademico con ID: {}", id);
            if (!repository.existsById(id)) {
                return new ApiResponse<>(404, "Estado de período académico no encontrado.", null);
            }
            repository.deleteById(id);
            return new ApiResponse<>(200, "Estado de período académico eliminado correctamente.", null);
        } catch (Exception e) {
            LOGGER.error("❌ Error al eliminar EstadoPeriodoAcademico con ID: {}", id, e);
            return new ApiResponse<>(500, "Error inesperado al eliminar el estado de período académico.", null);
        }
    }
}
