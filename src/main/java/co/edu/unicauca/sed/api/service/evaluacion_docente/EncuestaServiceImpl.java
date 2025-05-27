package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.domain.Encuesta;
import co.edu.unicauca.sed.api.domain.EvaluacionEstudiante;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.EvaluacionDocenteDTO;
import co.edu.unicauca.sed.api.repository.EncuestaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EncuestaServiceImpl implements EncuestaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncuestaServiceImpl.class);

    @Autowired
    private EncuestaRepository encuestaRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<Encuesta>> buscarTodos(Pageable pageable) {
        try {
            Page<Encuesta> encuestas = encuestaRepository.findAll(pageable);
            return new ApiResponse<>(200, "Encuestas obtenidas correctamente.", encuestas);
        } catch (Exception e) {
            LOGGER.error("❌ Error al listar encuestas", e);
            return new ApiResponse<>(500, "Error al listar encuestas: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Encuesta> buscarPorId(Integer oid) {
        try {
            Optional<Encuesta> encuesta = encuestaRepository.findById(oid);
            return encuesta.map(value -> new ApiResponse<>(200, "Encuesta encontrada.", value))
                    .orElseGet(() -> new ApiResponse<>(404, "Encuesta no encontrada.", null));
        } catch (Exception e) {
            LOGGER.error("❌ Error al buscar encuesta con ID: {}", oid, e);
            return new ApiResponse<>(500, "Error al buscar encuesta: " + e.getMessage(), null);
        }
    }

    public ApiResponse<Encuesta> guardar(Encuesta encuesta) {
        try {
            return new ApiResponse<>(200, "Encuesta guardada correctamente.", encuestaRepository.save(encuesta));
        } catch (Exception e) {
            LOGGER.error("❌ Error al guardar encuesta: {}", encuesta, e);
            return new ApiResponse<>(500, "Error al guardar encuesta: " + e.getMessage(), null);
        }
    }	

    public Encuesta guardarEncuesta(EvaluacionDocenteDTO dto, EvaluacionEstudiante evaluacionEstudiante) {
        Encuesta encuesta = encuestaRepository.findByEvaluacionEstudiante(evaluacionEstudiante)
                .orElse(new Encuesta());

        encuesta.setEvaluacionEstudiante(evaluacionEstudiante);
        encuesta.setNombre(dto.getEncuesta().getNombre());

        return encuestaRepository.save(encuesta);
    }

    @Override
    @Transactional
    public ApiResponse<Void> eliminar(Integer oid) {
        try {
            LOGGER.info("🗑️ Eliminando encuesta con ID: {}", oid);
            if (!encuestaRepository.existsById(oid)) {
                return new ApiResponse<>(404, "Encuesta no encontrada.", null);
            }
            encuestaRepository.deleteById(oid);
            return new ApiResponse<>(200, "Encuesta eliminada correctamente.", null);
        } catch (Exception e) {
            LOGGER.error("❌ Error al eliminar encuesta con ID: {}", oid, e);
            return new ApiResponse<>(500, "Error al eliminar encuesta: " + e.getMessage(), null);
        }
    }
}
