package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.ActividadDecimal;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.actividad.ActividadDecimalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

/**
 * Controlador REST para gestionar ACTIVIDADDECIMAL.
 */
@RestController
@RequestMapping("/api/actividaddecimal")
@RequiredArgsConstructor
public class ActividadDecimalController {

    private final ActividadDecimalService actividadDecimalService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActividadDecimal>>> listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return actividadDecimalService.obtenerTodos(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActividadDecimal>> buscarPorId(@PathVariable Integer id) {
        return actividadDecimalService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActividadDecimal>> crear(@RequestBody ActividadDecimal actividadDecimal) {
        return actividadDecimalService.crear(actividadDecimal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActividadDecimal>> actualizar(@PathVariable Integer id,
            @RequestBody ActividadDecimal actividadDecimal) {
        return actividadDecimalService.actualizar(id, actividadDecimal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        return actividadDecimalService.eliminar(id);
    }
}
