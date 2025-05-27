package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.EstadoFuente;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.repository.EstadoFuenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EstadoFuenteServiceImpl implements EstadoFuenteService {

    private static final Logger logger = LoggerFactory.getLogger(EstadoFuenteServiceImpl.class);

    @Autowired
    private EstadoFuenteRepository estadoFuenteRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<EstadoFuente>> buscarTodos(Pageable pageable) {
        try {
            Page<EstadoFuente> estados = estadoFuenteRepository.findAll(pageable);
            return new ApiResponse<>(200, "Lista de estados de fuente obtenida correctamente.", estados);
        } catch (Exception e) {
            logger.error("❌ Error al listar EstadosFuente", e);
            return new ApiResponse<>(500, "Error al listar estados de fuente: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<EstadoFuente> buscarPorId(Integer id) {
        try {
            Optional<EstadoFuente> estadoFuente = estadoFuenteRepository.findById(id);
            return estadoFuente.map(value ->
                    new ApiResponse<>(200, "Estado de fuente encontrado.", value))
                    .orElseGet(() -> new ApiResponse<>(404, "Estado de fuente no encontrado.", null));
        } catch (Exception e) {
            logger.error("❌ Error al buscar EstadoFuente con ID: {}", id, e);
            return new ApiResponse<>(500, "Error al buscar estado de fuente: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<EstadoFuente> crear(EstadoFuente estadoFuente) {
        try {
            estadoFuente.setNombreEstado(estadoFuente.getNombreEstado().toUpperCase());
            EstadoFuente nuevoEstado = estadoFuenteRepository.save(estadoFuente);
            return new ApiResponse<>(201, "Estado de fuente creado exitosamente.", nuevoEstado);
        } catch (Exception e) {
            logger.error("❌ Error al crear EstadoFuente", e);
            return new ApiResponse<>(500, "Error al crear estado de fuente: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<EstadoFuente> actualizar(Integer id, EstadoFuente estadoFuente) {
        try {
            logger.info("🔄 Actualizando EstadoFuente con ID: {}", id);
            return estadoFuenteRepository.findById(id).map(existing -> {
                existing.setNombreEstado(estadoFuente.getNombreEstado().toUpperCase());
                EstadoFuente actualizado = estadoFuenteRepository.save(existing);
                return new ApiResponse<>(200, "Estado de fuente actualizado correctamente.", actualizado);
            }).orElseGet(() -> new ApiResponse<>(404, "Estado de fuente no encontrado.", null));
        } catch (Exception e) {
            logger.error("❌ Error al actualizar EstadoFuente con ID: {}", id, e);
            return new ApiResponse<>(500, "Error al actualizar estado de fuente: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer id) {
        try {
            logger.info("🗑️ Eliminando EstadoFuente con ID: {}", id);
            if (!estadoFuenteRepository.existsById(id)) {
                return new ApiResponse<>(404, "Estado de fuente no encontrado.", null);
            }
            estadoFuenteRepository.deleteById(id);
            return new ApiResponse<>(200, "Estado de fuente eliminado correctamente.", null);
        } catch (Exception e) {
            logger.error("❌ Error al eliminar EstadoFuente con ID: {}", id, e);
            return new ApiResponse<>(500, "Error al eliminar estado de fuente: " + e.getMessage(), null);
        }
    }

    /**
     * Crea una nueva instancia de `EstadoFuente` con el ID especificado.
     *
     * @param oidEstado ID del estado fuente.
     * @return Instancia de EstadoFuente.
     */
    public EstadoFuente createEstadoFuente(int oidEstado) {
        EstadoFuente stateSource = new EstadoFuente();
        stateSource.setOidEstadoFuente(oidEstado);
        return stateSource;
    }
}
