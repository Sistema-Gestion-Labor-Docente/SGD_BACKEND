package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.ActividadBoolean;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.actividad.ActividadBooleanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/actividadboolean")
@RequiredArgsConstructor
@Tag(name = "Actividad Boolean", description = "Gestión de actividades booleanas del sistema")
public class ActividadBooleanController {

    private final ActividadBooleanService actividadBooleanService;

    @GetMapping
    @Operation(
        summary = "Listar actividades booleanas",
        description = "Obtiene una lista paginada de todas las actividades booleanas registradas."
    )
    public ResponseEntity<ApiResponse<Page<ActividadBoolean>>> listar(
            @Parameter(description = "Número de página (por defecto 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página (por defecto 10)")
            @RequestParam(defaultValue = "10") int size) {

        return actividadBooleanService.obtenerTodos(page, size);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar actividad booleana por ID",
        description = "Retorna una actividad booleana específica a partir de su ID."
    )
    public ResponseEntity<ApiResponse<ActividadBoolean>> buscarPorId(
            @Parameter(description = "ID de la actividad booleana") @PathVariable Integer id) {
        return actividadBooleanService.buscarPorId(id);
    }

    @PostMapping
    @Operation(
        summary = "Crear nueva actividad booleana",
        description = "Registra una nueva actividad booleana en el sistema."
    )
    public ResponseEntity<ApiResponse<ActividadBoolean>> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos de la actividad booleana a crear",
                required = true,
                content = @Content(schema = @Schema(implementation = ActividadBoolean.class))
            )
            @RequestBody ActividadBoolean actividadBoolean) {
        return actividadBooleanService.crear(actividadBoolean);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar actividad booleana",
        description = "Actualiza los datos de una actividad booleana existente."
    )
    public ResponseEntity<ApiResponse<ActividadBoolean>> actualizar(
            @Parameter(description = "ID de la actividad booleana a actualizar") @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos actualizados de la actividad",
                required = true,
                content = @Content(schema = @Schema(implementation = ActividadBoolean.class))
            )
            @RequestBody ActividadBoolean actividadBoolean) {
        return actividadBooleanService.actualizar(id, actividadBoolean);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar actividad booleana",
        description = "Elimina una actividad booleana del sistema a partir de su ID."
    )
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @Parameter(description = "ID de la actividad booleana a eliminar") @PathVariable Integer id) {
        return actividadBooleanService.eliminar(id);
    }
}
