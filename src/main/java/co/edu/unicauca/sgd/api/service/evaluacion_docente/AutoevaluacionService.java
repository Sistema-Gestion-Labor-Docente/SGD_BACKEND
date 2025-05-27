package co.edu.unicauca.sgd.api.service.evaluacion_docente;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.AutoevaluacionDTO;

public interface AutoevaluacionService {
    /**
     * Guarda o actualiza una autoevaluación con sus respectivos documentos adjuntos
     * y soportes por ODS.
     *
     * @param dto             Objeto con la información de la autoevaluación.
     * @param firma           Firma del docente.
     * @param screenshotSimca Captura de pantalla del SIMCA.
     * @param documentoNotas  Documento con las notas diligenciadas.
     * @param archivosOds     Archivos de evidencia por cada ODS (clave:ods-<oidOds>).
     * @return ApiResponse indicando el resultado de la operación.
     */
    ApiResponse<Void> guardarAutoevaluacion(AutoevaluacionDTO dto, MultipartFile firma, MultipartFile screenshotSimca, MultipartFile documentoAutoevaluacion, List<MultipartFile> archivosOds);

    ApiResponse<Object> listarAutoevaluacion(Integer oidFuente);
}
