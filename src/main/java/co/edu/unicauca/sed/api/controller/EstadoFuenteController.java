package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.EstadoFuente;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.fuente.EstadoFuenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para gestionar EstadoFuente.
 */
@RestController
@RequestMapping("/api/estado-fuente")
public class EstadoFuenteController {

    @Autowired
    private EstadoFuenteService estadoFuenteService;

    /**
     * Obtiene una lista paginada de EstadoFuente.
     *
     * @param pageable Configuración de paginación.
     * @return ApiResponse con la lista paginada.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EstadoFuente>>> obtenerTodos(Pageable pageable) {
        return ResponseEntity.ok(estadoFuenteService.buscarTodos(pageable));
    }

    /**
     * Obtiene un EstadoFuente por su ID.
     *
     * @param id ID del EstadoFuente.
     * @return ApiResponse con el EstadoFuente encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EstadoFuente>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(estadoFuenteService.buscarPorId(id));
    }

    /**
     * Crea un nuevo EstadoFuente.
     *
     * @param estadoFuente Objeto EstadoFuente a guardar.
     * @return ApiResponse con el EstadoFuente creado.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EstadoFuente>> crear(@RequestBody EstadoFuente estadoFuente) {
        return ResponseEntity.ok(estadoFuenteService.crear(estadoFuente));
    }

    /**
     * Actualiza un EstadoFuente existente.
     *
     * @param id ID del EstadoFuente a actualizar.
     * @param estadoFuente Objeto EstadoFuente con datos actualizados.
     * @return ApiResponse con el EstadoFuente actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EstadoFuente>> actualizar(@PathVariable Integer id, @RequestBody EstadoFuente estadoFuente) {
        return ResponseEntity.ok(estadoFuenteService.actualizar(id, estadoFuente));
    }

    /**
     * Elimina un EstadoFuente por su ID.
     *
     * @param id ID del EstadoFuente a eliminar.
     * @return ApiResponse indicando el resultado de la operación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(estadoFuenteService.eliminar(id));
    }
}
