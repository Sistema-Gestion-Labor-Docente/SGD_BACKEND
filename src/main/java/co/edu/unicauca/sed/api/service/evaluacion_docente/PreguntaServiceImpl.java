package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.domain.Pregunta;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.repository.PreguntaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Implementación del servicio para la gestión de preguntas.
 */
@Service
@RequiredArgsConstructor
public class PreguntaServiceImpl implements PreguntaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreguntaServiceImpl.class);

    private final PreguntaRepository preguntaRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<Pregunta>> obtenerTodos(Pageable pageable) {
        try {
            Page<Pregunta> preguntas = preguntaRepository.findAll(pageable);
            return preguntas.isEmpty()
                    ? new ApiResponse<>(204, "No se encontraron preguntas registradas.", Page.empty())
                    : new ApiResponse<>(200, "Preguntas obtenidas correctamente.", preguntas);
        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener preguntas paginadas", e);
            return new ApiResponse<>(500, "Error inesperado al obtener las preguntas.", Page.empty());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Pregunta> buscarPorOid(Integer oid) {
        try {
            Pregunta pregunta = preguntaRepository.findById(oid)
                    .orElseThrow(() -> new EntityNotFoundException("Pregunta con ID " + oid + " no encontrada."));
            return new ApiResponse<>(200, "Pregunta encontrada correctamente.", pregunta);
        } catch (EntityNotFoundException e) {
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            LOGGER.error("❌ Error al buscar Pregunta con OID: {}", oid, e);
            return new ApiResponse<>(500, "Error inesperado al buscar la pregunta.", null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Pregunta> guardar(Pregunta pregunta) {
        try {
            if (pregunta.getPregunta() != null) {
                pregunta.setPregunta(pregunta.getPregunta().toUpperCase());
            }
            Pregunta guardada = preguntaRepository.save(pregunta);
            return new ApiResponse<>(201, "Pregunta guardada correctamente.", guardada);
        } catch (Exception e) {
            LOGGER.error("❌ Error al guardar Pregunta", e);
            return new ApiResponse<>(500, "Error inesperado al guardar la pregunta.", null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<List<Pregunta>> guardarTodas(List<Pregunta> preguntas) {
        try {
            preguntas.forEach(p -> {
                if (p.getPregunta() != null) {
                    p.setPregunta(p.getPregunta().toUpperCase());
                }
            });
            List<Pregunta> guardadas = preguntaRepository.saveAll(preguntas);
            return new ApiResponse<>(201, "Preguntas guardadas correctamente.", guardadas);
        } catch (Exception e) {
            LOGGER.error("❌ Error al guardar múltiples Preguntas", e);
            return new ApiResponse<>(500, "Error inesperado al guardar las preguntas.", null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            LOGGER.info("🗑 Eliminando Pregunta con OID: {}", oid);
            if (!preguntaRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Pregunta no encontrada.", null);
            }
            preguntaRepository.deleteById(oid);
            return new ApiResponse<>(200, "Pregunta eliminada correctamente.", null);
        } catch (Exception e) {
            LOGGER.error("❌ Error al eliminar Pregunta con OID: {}", oid, e);
            return new ApiResponse<>(500, "Error inesperado al eliminar la pregunta.", null);
        }
    }
}
