package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.ActividadInt;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.actividad.ActividadIntService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

/**
 * Controlador REST para gestionar ACTIVIDADINT.
 */
@RestController
@RequestMapping("/api/actividadint")
@RequiredArgsConstructor
public class ActividadIntController {

    private final ActividadIntService actividadIntService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActividadInt>>> listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return actividadIntService.obtenerTodos(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActividadInt>> buscarPorId(@PathVariable Integer id) {
        return actividadIntService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActividadInt>> crear(@RequestBody ActividadInt actividadInt) {
        return actividadIntService.crear(actividadInt);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActividadInt>> actualizar(@PathVariable Integer id,
            @RequestBody ActividadInt actividadInt) {
        return actividadIntService.actualizar(id, actividadInt);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        return actividadIntService.eliminar(id);
    }
}
