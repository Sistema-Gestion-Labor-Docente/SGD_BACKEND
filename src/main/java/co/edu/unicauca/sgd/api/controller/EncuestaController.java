package co.edu.unicauca.sgd.api.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.domain.Encuesta;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.service.evaluacion_docente.EncuestaService;

/**
 * Controlador para la gestión de encuestas.
 */
@RestController
@RequestMapping("api/encuesta")
@RequiredArgsConstructor
public class EncuestaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncuestaController.class);
    private final EncuestaService encuestaService;

    /**
     * Recupera todas las encuestas disponibles con paginación.
     *
     * @param pageable Objeto de paginación.
     * @return Página de encuestas.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Encuesta>>> buscarTodos(Pageable pageable) {
        return ResponseEntity.ok(encuestaService.buscarTodos(pageable));
    }

    /**
     * Recupera una encuesta específica por su ID.
     *
     * @param oid ID de la encuesta.
     * @return Encuesta encontrada o mensaje de error si no existe.
     */
    @GetMapping("/{oid}")
    public ResponseEntity<ApiResponse<Encuesta>> buscarPorId(@PathVariable Integer oid) {
        return ResponseEntity.ok(encuestaService.buscarPorId(oid));
    }

    /**
     * Guarda una nueva encuesta.
     *
     * @param encuesta Objeto Encuesta a guardar.
     * @return Encuesta guardada o mensaje de error si ocurre algún problema.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Encuesta>> guardar(@RequestBody Encuesta encuesta) {
        return ResponseEntity.ok(encuestaService.guardar(encuesta));
    }

    /**
     * Elimina una encuesta por su ID.
     *
     * @param oid ID de la encuesta a eliminar.
     * @return Confirmación de eliminación o mensaje de error si ocurre un problema.
     */
    @DeleteMapping("/{oid}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer oid) {
        LOGGER.info("📌 Eliminando encuesta con ID: {}", oid);
        return ResponseEntity.ok(encuestaService.eliminar(oid));
    }
}
