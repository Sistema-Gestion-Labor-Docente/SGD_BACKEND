package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.actividad.*;
import co.edu.unicauca.sed.api.service.actividad.ActividadQueryService;
import co.edu.unicauca.sed.api.service.actividad.ActividadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;

@RestController
@RequestMapping("api/actividades")
@Tag(name = "Actividades", description = "Operaciones para la gestión de actividades")
public class ActividadController {

    private final ActividadService actividadService;
    private final ActividadQueryService actividadQueryService;

    public ActividadController(ActividadService actividadService, ActividadQueryService actividadQueryService) {
        this.actividadService = actividadService;
        this.actividadQueryService = actividadQueryService;
    }

    @GetMapping
    @Operation(
        summary = "Listar actividades",
        description = "Obtiene todas las actividades registradas con soporte de paginación y ordenamiento."
    )
    public ResponseEntity<ApiResponse<Page<ActividadBaseDTO>>> findAll(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Orden ascendente (true) o descendente (false)") @RequestParam(defaultValue = "true") boolean ascendingOrder) {
        ApiResponse<Page<ActividadBaseDTO>> response = actividadService.obtenerTodos(PageRequest.of(page, size), ascendingOrder);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(
        summary = "Buscar actividad por ID",
        description = "Consulta una actividad específica a partir de su identificador único."
    )
    public ResponseEntity<ApiResponse<ActividadBaseDTO>> findById(
            @Parameter(description = "ID de la actividad") @PathVariable Integer oid) {
        ApiResponse<ActividadBaseDTO> response = actividadService.buscarDTOPorId(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/buscarActividadesPorEvaluado")
    @Operation(
        summary = "Buscar actividades por evaluado",
        description = "Obtiene las actividades asignadas a un evaluado en períodos activos. Soporta múltiples filtros opcionales."
    )
    public ResponseEntity<ApiResponse<Page<ActividadBaseDTO>>> buscarActividadesPorEvaluado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer idEvaluador,
            @RequestParam(required = false) Integer idEvaluado,
            @RequestParam(required = false) String tipoActividad,
            @RequestParam(required = false) String nombreActividad,
            @RequestParam(required = false) String nombreEvaluador,
            @RequestParam(required = false) List<String> roles,
            @RequestParam(required = false) String tipoFuente,
            @RequestParam(required = false) String estadoFuente,
            @RequestParam(required = false) Boolean orden,
            @RequestParam(required = false) Integer idPeriodo,
            @RequestParam(required = false) Boolean asignacionDefault) {

        ApiResponse<Page<ActividadBaseDTO>> response = actividadQueryService.buscarActividadesPorEvaluado(
                idEvaluador, idEvaluado, nombreActividad, tipoActividad, nombreEvaluador,
                roles, tipoFuente, estadoFuente, orden, idPeriodo, asignacionDefault, PageRequest.of(page, size));

        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/buscarActividadesPorEvaluador")
    @Operation(
        summary = "Buscar actividades por evaluador",
        description = "Obtiene las actividades asignadas a un evaluador en períodos activos. Permite aplicar filtros por evaluado, tipo, nombre y estado."
    )
    public ResponseEntity<ApiResponse<Page<ActividadDTOEvaluador>>> buscarActividadesPorEvaluador(
            @RequestParam(required = false) Integer idEvaluador,
            @RequestParam(required = false) Integer idEvaluado,
            @RequestParam(required = false) String tipoActividad,
            @RequestParam(required = false) String nombreActividad,
            @RequestParam(required = false) String nombreEvaluado,
            @RequestParam(required = false) List<String> roles,
            @RequestParam(required = false) String tipoFuente,
            @RequestParam(required = false) String estadoFuente,
            @RequestParam(required = false) Boolean orden,
            @RequestParam(required = false) Integer idPeriodo,
            @RequestParam(required = false) Boolean asignacionDefault,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

            ApiResponse<Page<ActividadDTOEvaluador>> response = actividadQueryService.buscarActividadesPorEvaluador(
                idEvaluador, idEvaluado, nombreActividad, tipoActividad, nombreEvaluado, roles,
                tipoFuente, estadoFuente, orden, idPeriodo, asignacionDefault, PageRequest.of(page, size));     

        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(
        summary = "Guardar actividades",
        description = "Guarda una lista de actividades en una operación de carga masiva."
    )
    public ResponseEntity<ApiResponse<List<Actividad>>> save(
            @RequestBody(description = "Lista de actividades a guardar", required = true)
            @org.springframework.web.bind.annotation.RequestBody List<ActividadBaseDTO> actividadesDTO) {
        ApiResponse<List<Actividad>> response = actividadService.guardar(actividadesDTO);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{idActividad}")
    @Operation(
        summary = "Actualizar actividad",
        description = "Actualiza los datos de una actividad existente según su ID."
    )
    public ResponseEntity<ApiResponse<Actividad>> update(
            @Parameter(description = "ID de la actividad a actualizar") @PathVariable Integer idActividad,
            @RequestBody(description = "Datos actualizados de la actividad", required = true)
            @org.springframework.web.bind.annotation.RequestBody ActividadBaseDTO actividadDTO) {
        ApiResponse<Actividad> response = actividadService.actualizar(idActividad, actividadDTO);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(
        summary = "Eliminar actividad",
        description = "Elimina una actividad existente del sistema por su ID."
    )
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID de la actividad a eliminar") @PathVariable Integer oid) {
        ApiResponse<Void> response = actividadService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}
