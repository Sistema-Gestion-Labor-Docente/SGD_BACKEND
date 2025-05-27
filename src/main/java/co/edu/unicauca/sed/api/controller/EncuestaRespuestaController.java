package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.EncuestaRespuesta;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.EncuestaPreguntaDTO;
import co.edu.unicauca.sed.api.service.evaluacion_docente.EncuestaRespuestaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para la gestión de respuestas en encuestas.
 */
@RestController
@RequestMapping("api/encuesta-respuesta")
@RequiredArgsConstructor
public class EncuestaRespuestaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncuestaRespuestaController.class);
    private final EncuestaRespuestaService encuestaRespuestaService;

    /**
     * Recupera todas las respuestas de encuestas con paginación.
     *
     * @param pageable Objeto de paginación.
     * @return Página de respuestas de encuestas.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EncuestaRespuesta>>> buscarTodos(Pageable pageable) {
        return ResponseEntity.ok(encuestaRespuestaService.buscarTodos(pageable));
    }

    /**
     * Recupera una respuesta específica por su ID.
     *
     * @param oid ID de la respuesta.
     * @return Respuesta encontrada o mensaje de error si no existe.
     */
    @GetMapping("/{oid}")
    public ResponseEntity<ApiResponse<EncuestaRespuesta>> buscarPorId(@PathVariable Integer oid) {
        return ResponseEntity.ok(encuestaRespuestaService.buscarPorId(oid));
    }

    /**
     * Guarda o actualiza una respuesta en una encuesta.
     *
     * @param encuestaPreguntaDTO Datos de la respuesta.
     * @param oidEncuesta         ID de la encuesta.
     * @param oidPregunta         ID de la pregunta.
     * @return Respuesta guardada o mensaje de error si ocurre un problema.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EncuestaRespuesta>> guardar(
            @RequestBody EncuestaPreguntaDTO encuestaPreguntaDTO,
            @RequestParam Integer oidEncuesta,
            @RequestParam Integer oidPregunta) {
        return ResponseEntity.ok(encuestaRespuestaService.guardar(encuestaPreguntaDTO, oidEncuesta, oidPregunta));
    }

    /**
     * Elimina una respuesta en una encuesta por su ID.
     *
     * @param oid ID de la respuesta a eliminar.
     * @return Confirmación de eliminación o mensaje de error si ocurre un problema.
     */
    @DeleteMapping("/{oid}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer oid) {
        LOGGER.info("📌 Eliminando respuesta con ID: {}", oid);
        return ResponseEntity.ok(encuestaRespuestaService.eliminar(oid));
    }
}
