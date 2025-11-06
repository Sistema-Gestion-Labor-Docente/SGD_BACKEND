package co.edu.unicauca.sgd.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTOResponse;
import co.edu.unicauca.sgd.api.service.necesidad.AsignacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/necesidades/asignaciones")
@Tag(name = "Asignaciones", description = "Gestión de asignaciones docente-necesidad")
public class AsignacionController {

    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @GetMapping
    @Operation(summary = "Listar asignaciones")
    public ResponseEntity<ApiResponse<Page<AsignacionDTOResponse>>> listar(
            @RequestParam(required = false) Integer oidNecesidad,
            @RequestParam(required = false) Integer oidSeleccionado,
            Pageable pageable) {
        ApiResponse<Page<AsignacionDTOResponse>> response = asignacionService.listar(oidNecesidad, oidSeleccionado, pageable);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar asignación por ID")
    public ResponseEntity<ApiResponse<AsignacionDTOResponse>> buscarPorId(@PathVariable Integer oid) {
        ApiResponse<AsignacionDTOResponse> response = asignacionService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Crear asignación")
    public ResponseEntity<ApiResponse<AsignacionDTOResponse>> crear(@Valid @RequestBody AsignacionDTORequest request) {
        ApiResponse<AsignacionDTOResponse> response = asignacionService.crear(request);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PutMapping("/{oid}")
    @Operation(summary = "Actualizar asignación")
    public ResponseEntity<ApiResponse<AsignacionDTOResponse>> actualizar(
            @PathVariable Integer oid,
            @Valid @RequestBody AsignacionDTORequest request) {
        ApiResponse<AsignacionDTOResponse> response = asignacionService.actualizar(oid, request);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar asignación")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer oid) {
        ApiResponse<Void> response = asignacionService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }
}
