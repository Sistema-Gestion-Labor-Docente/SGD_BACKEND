package co.edu.unicauca.sgd.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.sgd.api.domain.EstadoConsolidado;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.service.consolidado.EstadoConsolidadoService;

import org.springframework.data.domain.Page;

/**
 * Controlador REST para gestionar ESTADOCONSOLIDADO.
 */
@RestController
@RequestMapping("/api/estadoconsolidado")
@RequiredArgsConstructor
public class EstadoConsolidadoController {

    private final EstadoConsolidadoService estadoConsolidadoService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EstadoConsolidado>>> listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return estadoConsolidadoService.obtenerTodos(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EstadoConsolidado>> buscarPorId(@PathVariable Integer id) {
        return estadoConsolidadoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EstadoConsolidado>> crear(@RequestBody EstadoConsolidado estado) {
        return estadoConsolidadoService.crear(estado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        return estadoConsolidadoService.eliminar(id);
    }
}
