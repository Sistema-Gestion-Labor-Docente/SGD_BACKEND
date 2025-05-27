package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.EstadoEtapaDesarrollo;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.evaluacion_docente.EstadoEtapaDesarrolloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

/**
 * Controlador REST para gestionar ESTADOETAPADESARROLLO.
 */
@RestController
@RequestMapping("/api/estadoetapadesarrollo")
@RequiredArgsConstructor
public class EstadoEtapaDesarrolloController {

    private final EstadoEtapaDesarrolloService estadoEtapaDesarrolloService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EstadoEtapaDesarrollo>>> listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return estadoEtapaDesarrolloService.obtenerTodos(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EstadoEtapaDesarrollo>> buscarPorId(@PathVariable Integer id) {
        return estadoEtapaDesarrolloService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EstadoEtapaDesarrollo>> crear(@RequestBody EstadoEtapaDesarrollo estado) {
        return estadoEtapaDesarrolloService.crear(estado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        return estadoEtapaDesarrolloService.eliminar(id);
    }
}
