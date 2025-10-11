package co.edu.unicauca.sgd.api.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTOResponse;
import co.edu.unicauca.sgd.api.service.materias.ProgramaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/programas")
@Tag(name = "Programa", description = "Gestión de programas académicos")
public class ProgramaController {

    private ProgramaService programaService;

    public ProgramaController(ProgramaService programaService) {
        this.programaService = programaService;
    }

    @GetMapping
    @Operation(summary = "Listar programas", description = "Obtiene todos los programas con filtros opcionales")
    public ResponseEntity<ApiResponse<Page<ProgramaDTOResponse>>> findAll(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {

        ApiResponse<Page<ProgramaDTOResponse>> response =
                programaService.obtenerTodos(nombre, pageable);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar programa por ID", description = "Consulta un programa por su ID")
    public ResponseEntity<ApiResponse<ProgramaDTOResponse>> findByOid(@PathVariable Integer oid) {
        ApiResponse<ProgramaDTOResponse> response = programaService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar programa", description = "Crea un nuevo programa")
    public ResponseEntity<ApiResponse<ProgramaDTOResponse>> save(@Valid @RequestBody ProgramaDTORequest dto) {
        ApiResponse<ProgramaDTOResponse> response = programaService.guardar(dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar programa", description = "Actualiza un programa existente")
    public ResponseEntity<ApiResponse<ProgramaDTOResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody ProgramaDTORequest dto) {
        ApiResponse<ProgramaDTOResponse> response = programaService.actualizar(id, dto);
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar programa", description = "Elimina un programa por su ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = programaService.eliminar(oid);
        if (response.getCodigo() >= 200 && response.getCodigo() < 300) {
            java.util.Map<String, Object> info = java.util.Map.of(
                "oid", oid,
                "mensaje", response.getMensaje()
            );
            return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                    .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), info));
        }
        return ResponseEntity.status(response.getCodigo() == 204 ? 200 : response.getCodigo())
                .body(new ApiResponse<>(response.getCodigo(), response.getMensaje(), null));
    }
}

