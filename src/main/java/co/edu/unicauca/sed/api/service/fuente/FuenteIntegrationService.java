package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.dto.AutoevaluacionDTO;
import co.edu.unicauca.sed.api.dto.EvaluacionDocenteDTO;
import co.edu.unicauca.sed.api.dto.FuenteCreateDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Interfaz para la integración de datos JSON y manejo de archivos adicionales en fuentes.
 */
public interface FuenteIntegrationService {

    /**
     * Convierte un JSON en una lista de DTOs de fuentes.
     *
     * @param fuentesJson JSON que contiene los datos de las fuentes.
     * @return Lista de objetos FuenteCreateDTO.
     */
    List<FuenteCreateDTO> convertirJsonAFuentes(String fuentesJson);

    /**
     * Filtra los archivos adicionales excluyendo el archivo fuente común.
     *
     * @param todosLosArchivos Mapa de todos los archivos a procesar.
     * @return Un mapa que contiene únicamente los archivos de informes ejecutivos.
     */
    Map<String, MultipartFile> filtrarArchivosEjecutivos(Map<String, MultipartFile> todosLosArchivos);

    /**
     * Convierte un JSON en un objeto EvaluacionDocenteDTO.
     *
     * @param evaluacionJson JSON que contiene los datos de la evaluación docente.
     * @return Objeto EvaluacionDocenteDTO.
     */
    EvaluacionDocenteDTO convertirJsonAEvaluacion(String evaluacionJson);
    
    AutoevaluacionDTO convertirJsonAAutoevaluacion(String evaluacionJson);
}
