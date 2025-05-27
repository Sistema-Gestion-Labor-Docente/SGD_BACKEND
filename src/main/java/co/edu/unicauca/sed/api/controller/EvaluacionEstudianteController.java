package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.EvaluacionEstudiante;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.EvaluacionDocenteDTO;
import co.edu.unicauca.sed.api.dto.PeriodoEvaluacionDTO;
import co.edu.unicauca.sed.api.service.evaluacion_docente.EvaluacionEstudianteService;
import co.edu.unicauca.sed.api.service.fuente.FuenteIntegrationService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador para la gestión de evaluaciones de estudiantes.
 */
@RestController
@RequestMapping("api/evaluacion-estudiante")
@RequiredArgsConstructor
public class EvaluacionEstudianteController {

    private final EvaluacionEstudianteService evaluacionEstudianteService;

    @Autowired
    private FuenteIntegrationService integrationService;

    /**
     * Recupera todas las evaluaciones de estudiantes con paginación.
     *
     * @param pageable Objeto de paginación.
     * @return Página de evaluaciones de estudiantes.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EvaluacionEstudiante>>> buscarTodos(Pageable pageable) {
        return ResponseEntity.ok(evaluacionEstudianteService.buscarTodos(pageable));
    }

    /**
     * Guarda una evaluación de estudiante.
     *
     * @param dto Datos de la evaluación.
     * @return Confirmación de guardado.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> guardarEvaluacionDocente(
            @RequestParam("data") String evaluacionJson,
            @RequestParam(value = "documentoFuente", required = false) MultipartFile documentoFuente,
            @RequestParam(value = "firmaEstudiante", required = false) MultipartFile firmaEstudiante) {

        EvaluacionDocenteDTO dto = integrationService.convertirJsonAEvaluacion(evaluacionJson);

        // Guardar Evaluación con documentos
        ApiResponse<Void> response = evaluacionEstudianteService.guardarEvaluacionDocente(dto, documentoFuente, firmaEstudiante);

        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/fuente/{oidFuente}")
    public ResponseEntity<ApiResponse<Object>> obtenerEvaluacionEstudiante(@PathVariable Integer oidFuente) {
        return ResponseEntity.ok(evaluacionEstudianteService.obtenerEvaluacionEstudiante(oidFuente));
    }

    @GetMapping("/respuesta")
    public ResponseEntity<ApiResponse<List<PeriodoEvaluacionDTO>>> obtenerEvaluacionesEstructuradas() {
        return ResponseEntity.ok(evaluacionEstudianteService.obtenerEvaluacionesEstructuradas());
    }
}
