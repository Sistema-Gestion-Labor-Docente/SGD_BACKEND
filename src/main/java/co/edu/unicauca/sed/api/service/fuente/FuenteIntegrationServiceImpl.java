package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.dto.AutoevaluacionDTO;
import co.edu.unicauca.sed.api.dto.EvaluacionDocenteDTO;
import co.edu.unicauca.sed.api.dto.FuenteCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para integrar datos JSON y manejar archivos adicionales en fuentes.
 */
@Service
public class FuenteIntegrationServiceImpl implements FuenteIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(FuenteIntegrationServiceImpl.class);

    @Override
    public List<FuenteCreateDTO> convertirJsonAFuentes(String fuentesJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(fuentesJson, new TypeReference<List<FuenteCreateDTO>>() {});
        } catch (IOException e) {
            logger.error("Error al convertir el JSON de fuentes a objetos DTO", e);
            throw new RuntimeException("Error al procesar el JSON de fuentes: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, MultipartFile> filtrarArchivosEjecutivos(Map<String, MultipartFile> todosLosArchivos) {
        try {
            if (todosLosArchivos == null || todosLosArchivos.isEmpty()) {
                logger.warn("El mapa de archivos adicionales está vacío o es nulo.");
                return Map.of();
            }

            return todosLosArchivos.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals("informeFuente"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        } catch (Exception e) {
            logger.error("Error al filtrar los archivos adicionales", e);
            throw new RuntimeException("Error al filtrar los archivos adicionales: " + e.getMessage(), e);
        }
    }

    @Override
    public EvaluacionDocenteDTO convertirJsonAEvaluacion(String evaluacionJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(evaluacionJson, EvaluacionDocenteDTO.class);
        } catch (Exception e) {
            logger.error("❌ Error al convertir JSON a EvaluacionDocenteDTO: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar la evaluación: JSON inválido.");
        }
    }

    @Override
    public AutoevaluacionDTO convertirJsonAAutoevaluacion(String evaluacionJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(evaluacionJson, AutoevaluacionDTO.class);
        } catch (Exception e) {
            logger.error("❌ Error al convertir JSON a AutoevaluacionDTO: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar la evaluación: JSON inválido.");
        }
    }
}
