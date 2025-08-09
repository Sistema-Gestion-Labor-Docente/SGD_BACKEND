package co.edu.unicauca.sgd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTOResponse;
import co.edu.unicauca.sgd.api.service.materias.PlanService;

@RestController
@RequestMapping("api/planes")
@Tag(name = "Plan", description = "Gestión de planes de estudio")
public class PlanController {

    private PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    @Operation(summary = "Listar planes", description = "Obtiene todos los planes con filtros opcionales")
    public ResponseEntity<ApiResponse<Page<PlanDTOResponse>>> findAll(
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer oidPrograma,
            Pageable pageable) {

        ApiResponse<Page<PlanDTOResponse>> response =
                planService.obtenerTodos(numero, estado, oidPrograma, pageable);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar plan por ID", description = "Consulta un plan por su ID")
    public ResponseEntity<ApiResponse<PlanDTOResponse>> findByOid(@PathVariable Integer oid) {
        ApiResponse<PlanDTOResponse> response = planService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar plan", description = "Crea un nuevo plan")
    public ResponseEntity<ApiResponse<PlanDTOResponse>> save(@Valid @RequestBody PlanDTORequest dto) {
        ApiResponse<PlanDTOResponse> response = planService.guardar(dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar plan", description = "Actualiza un plan existente")
    public ResponseEntity<ApiResponse<PlanDTOResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody PlanDTORequest dto) {
        ApiResponse<PlanDTOResponse> response = planService.actualizar(id, dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar plan", description = "Elimina un plan por su ID")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = planService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}
