package co.edu.unicauca.sgd.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.service.calendario.FechaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/fechas")
@Tag(name = "Fecha", description = "Gestión de fechas del calendario académico")
public class FechaController {

    private FechaService fechaService;

    public FechaController(FechaService fechaService) {
        this.fechaService = fechaService;
    }

    @GetMapping
    @Operation(summary = "Listar fechas", description = "Obtiene todas las fechas con filtros opcionales")
    public ResponseEntity<ApiResponse<Page<FechaDTOResponse>>> findAll(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) TipoFechaEnum tipo,
            Pageable pageable) {
        ApiResponse<Page<FechaDTOResponse>> response = fechaService.obtenerTodas(nombre, tipo, pageable);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar fecha por ID", description = "Consulta una fecha específica por su ID")
    public ResponseEntity<ApiResponse<FechaDTOResponse>> findByOid(@PathVariable Integer oid) {
        ApiResponse<FechaDTOResponse> response = fechaService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar fecha", description = "Guarda una nueva fecha")
    public ResponseEntity<ApiResponse<FechaDTOResponse>> save(@Valid @RequestBody FechaDTORequest dto) {
        ApiResponse<FechaDTOResponse> response = fechaService.guardar(dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar fecha", description = "Actualiza una fecha existente")
    public ResponseEntity<ApiResponse<FechaDTOResponse>> update(@PathVariable Integer id, @Valid @RequestBody FechaDTORequest dto) {
        ApiResponse<FechaDTOResponse> response = fechaService.actualizar(id, dto);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar fecha", description = "Elimina una fecha por su ID")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = fechaService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}


