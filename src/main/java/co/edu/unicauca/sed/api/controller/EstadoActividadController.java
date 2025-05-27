package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.EstadoActividad;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.actividad.EstadoActividadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/estado-actividad")
public class EstadoActividadController {

    private final EstadoActividadService service;

    public EstadoActividadController(EstadoActividadService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EstadoActividad>> create(@RequestBody EstadoActividad estadoActividad) {
        return service.guardar(estadoActividad);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EstadoActividad>> findById(@PathVariable Integer id) {
        return service.buscarPorOid(id);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EstadoActividad>>> findAll(Pageable pageable) {
        return service.obtenerTodos(pageable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EstadoActividad>> update(@PathVariable Integer id, @RequestBody EstadoActividad estadoActividad) {
        return service.actualizar(id, estadoActividad);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        return service.eliminar(id);
    }
}
