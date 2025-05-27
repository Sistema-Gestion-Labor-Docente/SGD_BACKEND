package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.ActividadDate;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.actividad.ActividadDateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

/**
 * Controlador REST para gestionar ACTIVIDADDATE.
 */
@RestController
@RequestMapping("/api/actividaddate")
@RequiredArgsConstructor
public class ActividadDateController {

    private final ActividadDateService actividadDateService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActividadDate>>> listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return actividadDateService.obtenerTodos(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActividadDate>> buscarPorId(@PathVariable Integer id) {
        return actividadDateService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActividadDate>> crear(@RequestBody ActividadDate actividadDate) {
        return actividadDateService.crear(actividadDate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActividadDate>> actualizar(@PathVariable Integer id,
            @RequestBody ActividadDate actividadDate) {
        return actividadDateService.actualizar(id, actividadDate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        return actividadDateService.eliminar(id);
    }
}
