package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.ActividadVarchar;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.actividad.ActividadVarcharService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

/**
 * Controlador REST para gestionar ACTIVIDADVARCHAR.
 */
@RestController
@RequestMapping("/api/actividadvarchar")
@RequiredArgsConstructor
public class ActividadVarcharController {

    private final ActividadVarcharService actividadVarcharService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActividadVarchar>>> listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return actividadVarcharService.obtenerTodos(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActividadVarchar>> buscarPorId(@PathVariable Integer id) {
        return actividadVarcharService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActividadVarchar>> crear(@RequestBody ActividadVarchar actividadVarchar) {
        return actividadVarcharService.crear(actividadVarchar);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActividadVarchar>> actualizar(@PathVariable Integer id,
            @RequestBody ActividadVarchar actividadVarchar) {
        return actividadVarcharService.actualizar(id, actividadVarchar);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        return actividadVarcharService.eliminar(id);
    }
}
