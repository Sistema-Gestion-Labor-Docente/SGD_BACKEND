package co.edu.unicauca.sgd.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import co.edu.unicauca.sgd.api.service.calendario.FechaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/fechas")
@Tag(name = "Fecha", description = "Gestión de fechas del calendario académico")
public class FechaController {

    @Autowired
    private FechaService fechaService;

    @GetMapping
    @Operation(summary = "Listar fechas", description = "Obtiene todas las fechas con filtros opcionales")
    public ResponseEntity<ApiResponse<Page<Fecha>>> findAll(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) TipoFechaEnum tipo,
            Pageable pageable) {
        ApiResponse<Page<Fecha>> response = fechaService.obtenerTodas(nombre, tipo, pageable);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar fecha por ID", description = "Consulta una fecha específica por su ID")
    public ResponseEntity<ApiResponse<Fecha>> findByOid(@PathVariable Integer oid) {
        ApiResponse<Fecha> response = fechaService.buscarPorId(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PostMapping
    @Operation(summary = "Guardar fecha", description = "Guarda una nueva fecha")
    public ResponseEntity<ApiResponse<Fecha>> save(@RequestBody Fecha fecha) {
        ApiResponse<Fecha> response = fechaService.guardar(fecha);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar fecha", description = "Actualiza una fecha existente")
    public ResponseEntity<ApiResponse<Fecha>> update(@PathVariable Integer id, @RequestBody Fecha fecha) {
        ApiResponse<Fecha> response = fechaService.actualizar(id, fecha);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar fecha", description = "Elimina una fecha por su ID")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer oid) {
        ApiResponse<Void> response = fechaService.eliminar(oid);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }
}

