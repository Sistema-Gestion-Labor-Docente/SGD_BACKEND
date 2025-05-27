package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.Resolucion;
import co.edu.unicauca.sed.api.service.proceso.ResolucionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/resolucion")
@Tag(name = "Resolución", description = "Gestión de resoluciones del sistema")
public class ResolucionController {

    private static final Logger logger = LoggerFactory.getLogger(ResolucionController.class);

    @Autowired
    private ResolucionService resolucionService;

    @GetMapping
    @Operation(summary = "Listar resoluciones", description = "Obtiene todas las resoluciones registradas")
    public ResponseEntity<?> findAll() {
        try {
            List<Resolucion> list = resolucionService.findAll();
            if (list != null && !list.isEmpty()) {
                return ResponseEntity.ok().body(list);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar resolución por ID", description = "Recupera una resolución específica por su ID")
    public ResponseEntity<?> find(
            @Parameter(description = "ID de la resolución") @PathVariable Integer oid) {
        Resolucion resolucion = this.resolucionService.findByOid(oid);
        if (resolucion != null) {
            return ResponseEntity.ok().body(resolucion);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crear resolución", description = "Registra una nueva resolución en el sistema")
    public ResponseEntity<?> save(
            @RequestBody(description = "Datos de la resolución a guardar", required = true)
            @org.springframework.web.bind.annotation.RequestBody Resolucion resolucion) {
        try {
            Resolucion resultado = resolucionService.save(resolucion);
            if (resultado != null) {
                return ResponseEntity.ok().body(resultado);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
        return ResponseEntity.internalServerError().body("Error: Resultado nulo");
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar resolución", description = "Elimina una resolución por su ID")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID de la resolución a eliminar") @PathVariable Integer oid) {
        Resolucion resolucion = null;
        try {
            resolucion = this.resolucionService.findByOid(oid);
            if (resolucion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resolución no encontrada");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resolución no encontrada");
        }

        try {
            this.resolucionService.delete(oid);
        } catch (Exception e) {
            logger.error("Error al eliminar resolución: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede borrar por conflictos con otros datos");
        }
        return ResponseEntity.ok().build();
    }
}
