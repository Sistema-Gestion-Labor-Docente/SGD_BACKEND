package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.Rol;
import co.edu.unicauca.sed.api.service.RolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/roles")
@Tag(name = "Roles", description = "Gestión de roles en el sistema")
public class RolController {

    private static final Logger logger = LoggerFactory.getLogger(RolController.class);

    @Autowired
    private RolService rolService;

    @PostMapping
    @Operation(summary = "Crear rol", description = "Crea un nuevo rol en el sistema")
    public ResponseEntity<Rol> create(
            @RequestBody(description = "Datos del nuevo rol", required = true)
            @org.springframework.web.bind.annotation.RequestBody Rol rol) {
        return new ResponseEntity<>(rolService.save(rol), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar rol por ID", description = "Recupera un rol específico por su ID")
    public ResponseEntity<Rol> findById(
            @Parameter(description = "ID del rol a buscar") @PathVariable Integer id) {
        return rolService.findByOid(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    logger.error("Rol no encontrado con id: {}", id);
                    return new RuntimeException("Rol no encontrado con id: " + id);
                });
    }

    @GetMapping
    @Operation(summary = "Listar roles", description = "Obtiene todos los roles registrados con paginación")
    public ResponseEntity<Page<Rol>> findAll(
            @Parameter(description = "Parámetros de paginación")
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(rolService.findAll(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol", description = "Actualiza un rol existente")
    public ResponseEntity<Rol> update(
            @Parameter(description = "ID del rol a actualizar") @PathVariable Integer id,
            @RequestBody(description = "Datos actualizados del rol", required = true)
            @org.springframework.web.bind.annotation.RequestBody Rol rol) {
        logger.info("Solicitud recibida para actualizar un rol con id: {}", id);
        return ResponseEntity.ok(rolService.update(id, rol));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol", description = "Elimina un rol por su ID")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del rol a eliminar") @PathVariable Integer id) {
        logger.info("Solicitud recibida para eliminar un rol con id: {}", id);
        rolService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        logger.error("Manejando excepción: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}
