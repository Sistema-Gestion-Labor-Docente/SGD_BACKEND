package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.TipoActividad;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.actividad.TipoActividadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/tipo-actividad")
@Tag(name = "Tipo Actividad", description = "Gestión de tipos de actividad")
public class TipoActividadController {

    private static final Logger logger = LoggerFactory.getLogger(TipoActividadController.class);

    @Autowired
    private TipoActividadService service;

    @PostMapping
    @Operation(summary = "Crear tipo de actividad", description = "Crea un nuevo tipo de actividad en el sistema")
    public ResponseEntity<ApiResponse<TipoActividad>> create(
            @RequestBody(description = "Datos del tipo de actividad", required = true)
            @org.springframework.web.bind.annotation.RequestBody TipoActividad tipoActividad) {
        ApiResponse<TipoActividad> response = service.guardar(tipoActividad);
        return ResponseEntity.status(response.getCodigo()).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de actividad por ID", description = "Recupera un tipo de actividad por su ID")
    public ResponseEntity<TipoActividad> findById(
            @Parameter(description = "ID del tipo de actividad") @PathVariable Integer id) {
        TipoActividad tipoActividad = service.buscarPorOid(id);
        if (tipoActividad != null) {
            return ResponseEntity.ok(tipoActividad);
        } else {
            logger.error("TipoActividad no encontrado con ID: {}", id);
            throw new RuntimeException("TipoActividad no encontrado con ID: " + id);
        }
    }

    @GetMapping
    @Operation(summary = "Listar tipos de actividad", description = "Obtiene todos los tipos de actividad con paginación")
    public ResponseEntity<ApiResponse<Page<TipoActividad>>> buscarTodos(Pageable pageable) {
        return ResponseEntity.ok(service.listarTodos(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de actividad", description = "Actualiza un tipo de actividad existente")
    public ResponseEntity<TipoActividad> update(
            @Parameter(description = "ID del tipo de actividad a actualizar") @PathVariable Integer id,
            @RequestBody(description = "Datos actualizados del tipo de actividad", required = true)
            @org.springframework.web.bind.annotation.RequestBody TipoActividad tipoActividad) {
        logger.info("Solicitud para actualizar TipoActividad con ID: {}", id);
        TipoActividad updatedTipoActividad = service.actualizar(id, tipoActividad);
        return ResponseEntity.ok(updatedTipoActividad);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de actividad", description = "Elimina un tipo de actividad por su ID")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del tipo de actividad a eliminar") @PathVariable Integer id) {
        logger.info("Solicitud para eliminar TipoActividad con ID: {}", id);
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        logger.error("Manejando excepción: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}
