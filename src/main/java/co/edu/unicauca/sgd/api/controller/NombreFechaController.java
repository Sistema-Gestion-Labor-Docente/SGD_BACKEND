package co.edu.unicauca.sgd.api.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTOResponse;
import co.edu.unicauca.sgd.api.service.calendario.NombreFechaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/nombre-fechas")
@Tag(name = "NombreFecha", description = "Catálogo de nombres de fecha")
public class NombreFechaController {

    private final NombreFechaService nombreFechaService;

    public NombreFechaController(NombreFechaService nombreFechaService) {
        this.nombreFechaService = nombreFechaService;
    }

    @GetMapping
    @Operation(summary = "Listar", description = "Lista con filtro opcional por nombre")
    public ResponseEntity<ApiResponse<Page<NombreFechaDTOResponse>>> listar(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        ApiResponse<Page<NombreFechaDTOResponse>> resp = nombreFechaService.obtenerTodas(nombre, pageable);
        return ResponseEntity.status(resp.getCodigo() == 204 ? 200 : resp.getCodigo()).body(resp);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar por ID")
    public ResponseEntity<ApiResponse<NombreFechaDTOResponse>> buscarPorId(@PathVariable Integer oid) {
        ApiResponse<NombreFechaDTOResponse> resp = nombreFechaService.buscarPorId(oid);
        return ResponseEntity.status(resp.getCodigo() == 204 ? 200 : resp.getCodigo()).body(resp);
    }

    @PostMapping
    @Operation(summary = "Crear")
    public ResponseEntity<ApiResponse<NombreFechaDTOResponse>> crear(@Valid @RequestBody NombreFechaDTORequest dto) {
        ApiResponse<NombreFechaDTOResponse> resp = nombreFechaService.guardar(dto);
        return ResponseEntity.status(resp.getCodigo() == 204 ? 200 : resp.getCodigo()).body(resp);
    }

    @PutMapping("/{oid}")
    @Operation(summary = "Actualizar")
    public ResponseEntity<ApiResponse<NombreFechaDTOResponse>> actualizar(@PathVariable Integer oid,
            @Valid @RequestBody NombreFechaDTORequest dto) {
        ApiResponse<NombreFechaDTOResponse> resp = nombreFechaService.actualizar(oid, dto);
        return ResponseEntity.status(resp.getCodigo() == 204 ? 200 : resp.getCodigo()).body(resp);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> eliminar(@PathVariable Integer oid) {
        ApiResponse<Void> resp = nombreFechaService.eliminar(oid);
        if (resp.getCodigo() >= 200 && resp.getCodigo() < 300) {
            java.util.Map<String, Object> info = java.util.Map.of(
                "oid", oid,
                "mensaje", resp.getMensaje()
            );
            return ResponseEntity.status(resp.getCodigo() == 204 ? 200 : resp.getCodigo())
                    .body(new ApiResponse<>(resp.getCodigo(), resp.getMensaje(), info));
        }
        return ResponseEntity.status(resp.getCodigo() == 204 ? 200 : resp.getCodigo())
                .body(new ApiResponse<>(resp.getCodigo(), resp.getMensaje(), null));
    }
}

