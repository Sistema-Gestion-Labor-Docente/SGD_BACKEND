package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.Proceso;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.proceso.ProcesoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

@RestController
@RequestMapping("api/proceso")
@Tag(name = "Proceso", description = "Gestión de procesos de evaluación")
public class ProcesoController {

    @Autowired
    private ProcesoService procesoService;

    @GetMapping
    @Operation(summary = "Listar procesos", description = "Obtiene todos los procesos con filtros y paginación")
    public ResponseEntity<ApiResponse<Page<Proceso>>> findAll(
            @RequestParam(required = false) Integer idEvaluador,
            @RequestParam(required = false) Integer idEvaluado,
            @RequestParam(required = false) Integer idPeriodo,
            @RequestParam(required = false) String nombreProceso,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaCreacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaActualizacion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ApiResponse<Page<Proceso>> response = procesoService.buscarTodos(
                idEvaluador, idEvaluado, idPeriodo, nombreProceso, fechaCreacion, fechaActualizacion, PageRequest.of(page, size));

        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar proceso por ID", description = "Recupera un proceso específico por su ID")
    public ResponseEntity<ApiResponse<Proceso>> findById(
            @Parameter(description = "ID del proceso") @PathVariable Integer oid) {
        ApiResponse<Proceso> response = procesoService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Crear proceso", description = "Registra un nuevo proceso en el sistema")
    public ResponseEntity<ApiResponse<Proceso>> save(
            @RequestBody(description = "Datos del proceso a guardar", required = true)
            @org.springframework.web.bind.annotation.RequestBody Proceso proceso) {
        ApiResponse<Proceso> response = procesoService.guardar(proceso);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{oid}")
    @Operation(summary = "Actualizar proceso", description = "Actualiza un proceso existente")
    public ResponseEntity<ApiResponse<Proceso>> update(
            @Parameter(description = "ID del proceso a actualizar") @PathVariable Integer oid,
            @RequestBody(description = "Datos actualizados del proceso", required = true)
            @org.springframework.web.bind.annotation.RequestBody Proceso proceso) {
        ApiResponse<Proceso> response = procesoService.actualizar(oid, proceso);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar proceso", description = "Elimina un proceso por su ID")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID del proceso a eliminar") @PathVariable Integer oid) {
        ApiResponse<Void> response = procesoService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}
