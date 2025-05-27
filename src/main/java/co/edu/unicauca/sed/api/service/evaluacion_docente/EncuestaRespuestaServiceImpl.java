package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.domain.Encuesta;
import co.edu.unicauca.sed.api.domain.EncuestaRespuesta;
import co.edu.unicauca.sed.api.domain.Pregunta;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.EncuestaPreguntaDTO;
import co.edu.unicauca.sed.api.repository.EncuestaRespuestaRepository;
import co.edu.unicauca.sed.api.repository.EncuestaRepository;
import co.edu.unicauca.sed.api.repository.PreguntaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EncuestaRespuestaServiceImpl implements EncuestaRespuestaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncuestaRespuestaServiceImpl.class);

    @Autowired
    private EncuestaRespuestaRepository encuestaPreguntaRepository;

    @Autowired
    private EncuestaRepository encuestaRepository;

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<EncuestaRespuesta>> buscarTodos(Pageable pageable) {
        try {
            Page<EncuestaRespuesta> encuestaPreguntas = encuestaPreguntaRepository.findAll(pageable);
            return new ApiResponse<>(200, "Encuesta-Pregunta obtenidas correctamente.", encuestaPreguntas);
        } catch (Exception e) {
            LOGGER.error("❌ Error al listar EncuestaRespuesta", e);
            return new ApiResponse<>(500, "Error al listar EncuestaRespuesta: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<EncuestaRespuesta> buscarPorId(Integer oid) {
        try {
            return encuestaPreguntaRepository.findById(oid)
                .map(encuestaPregunta -> new ApiResponse<>(200, "Encuesta-Pregunta encontrada.", encuestaPregunta))
                .orElseGet(() -> new ApiResponse<>(404, "Encuesta-Pregunta no encontrada.", null));
        } catch (Exception e) {
            LOGGER.error("❌ Error al buscar EncuestaRespuesta con ID: {}", oid, e);
            return new ApiResponse<>(500, "Error al buscar EncuestaRespuesta: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<EncuestaRespuesta> guardar(EncuestaPreguntaDTO encuestaPreguntaDTO, Integer oidEncuesta,
            Integer oidPregunta) {
        try {
            Encuesta encuesta = encuestaRepository.findById(oidEncuesta)
                .orElseThrow(() -> new EntityNotFoundException("Encuesta con ID " + oidEncuesta + " no encontrada."));

            Pregunta pregunta = preguntaRepository.findById(oidPregunta)
                .orElseThrow(() -> new EntityNotFoundException("Pregunta con ID " + oidPregunta + " no encontrada."));

            EncuestaRespuesta encuestaPregunta = encuestaPreguntaRepository
                .findByEncuestaAndPregunta(encuesta, pregunta).orElse(new EncuestaRespuesta());

            encuestaPregunta.setEncuesta(encuesta);
            encuestaPregunta.setPregunta(pregunta);
            encuestaPregunta.setRespuesta(encuestaPreguntaDTO.getRespuesta());

            encuestaPreguntaRepository.save(encuestaPregunta);
            return new ApiResponse<>(201, "EncuestaRespuesta guardada correctamente.", encuestaPregunta);
        } catch (EntityNotFoundException e) {
            LOGGER.warn("⚠️ {}", e.getMessage());
            return new ApiResponse<>(404, e.getMessage(), null);
        } catch (Exception e) {
            LOGGER.error("❌ Error al guardar EncuestaRespuesta", e);
            return new ApiResponse<>(500, "Error al guardar EncuestaRespuesta: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            LOGGER.info("🗑️ Eliminando EncuestaRespuesta con ID: {}", oid);
            if (!encuestaPreguntaRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Encuesta-Pregunta no encontrada.", null);
            }
            encuestaPreguntaRepository.deleteById(oid);
            return new ApiResponse<>(200, "Encuesta-Pregunta eliminada correctamente.", null);
        } catch (Exception e) {
            LOGGER.error("❌ Error al eliminar EncuestaRespuesta con ID: {}", oid, e);
            return new ApiResponse<>(500, "Error al eliminar EncuestaRespuesta: " + e.getMessage(), null);
        }
    }
}
