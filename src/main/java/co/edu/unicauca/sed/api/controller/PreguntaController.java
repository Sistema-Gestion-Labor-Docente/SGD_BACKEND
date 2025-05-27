package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.Pregunta;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.evaluacion_docente.PreguntaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Controlador para la gestión de preguntas.
 */
@RestController
@RequestMapping("api/pregunta")
@RequiredArgsConstructor
@Tag(name = "Preguntas", description = "Operaciones para la gestión de preguntas de evaluación")
public class PreguntaController {

    private final PreguntaService preguntaService;

    @GetMapping
    @Operation(
        summary = "Listar preguntas",
        description = "Obtiene todas las preguntas registradas en el sistema con soporte de paginación."
    )
    public ResponseEntity<ApiResponse<Page<Pregunta>>> obtenerTodos(Pageable pageable) {
        return ResponseEntity.ok(preguntaService.obtenerTodos(pageable));
    }

    @GetMapping("/{oid}")
    @Operation(
        summary = "Buscar pregunta por ID",
        description = "Consulta una pregunta específica a partir de su identificador único (OID)."
    )
    public ResponseEntity<ApiResponse<Pregunta>> obtenerPorOid(
            @Parameter(description = "ID de la pregunta") @PathVariable Integer oid) {
        return ResponseEntity.ok(preguntaService.buscarPorOid(oid));
    }

    @PostMapping
    @Operation(
        summary = "Guardar pregunta",
        description = "Guarda una nueva pregunta en la base de datos."
    )
    public ResponseEntity<ApiResponse<Pregunta>> guardar(
            @RequestBody(description = "Pregunta a guardar", required = true)
            @org.springframework.web.bind.annotation.RequestBody Pregunta pregunta) {
        return ResponseEntity.ok(preguntaService.guardar(pregunta));
    }

    @PostMapping("guardarTodas")
    @Operation(
        summary = "Guardar varias preguntas",
        description = "Guarda una lista de preguntas en la base de datos."
    )
    public ResponseEntity<ApiResponse<List<Pregunta>>> guardarTodas(
            @RequestBody(description = "Lista de preguntas a guardar", required = true)
            @org.springframework.web.bind.annotation.RequestBody List<Pregunta> preguntas) {
        return ResponseEntity.ok(preguntaService.guardarTodas(preguntas));
    }

    @DeleteMapping("/{oid}")
    @Operation(
        summary = "Eliminar pregunta",
        description = "Elimina una pregunta existente a partir de su identificador único (OID)."
    )
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID de la pregunta a eliminar") @PathVariable Integer oid) {
        return ResponseEntity.ok(preguntaService.eliminar(oid));
    }
}
