package co.edu.unicauca.sed.api.controller;

import java.util.List;

import co.edu.unicauca.sed.api.domain.Oficio;
import co.edu.unicauca.sed.api.service.proceso.OficioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para la gestión de oficios.
 */
@Controller
@RequestMapping("api/oficio")
@Tag(name = "Oficios", description = "Operaciones para la gestión de oficios asociados a los procesos")
public class OficioController {

    @Autowired
    private OficioService oficioService;

    @GetMapping
    @Operation(
        summary = "Listar oficios",
        description = "Retorna todos los oficios registrados en el sistema."
    )
    public ResponseEntity<List<Oficio>> findAll() {
        try {
            List<Oficio> list = oficioService.findAll();
            if (list != null && !list.isEmpty()) {
                return ResponseEntity.ok(list);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{oid}")
    @Operation(
        summary = "Buscar oficio por ID",
        description = "Obtiene un oficio específico a partir de su identificador único."
    )
    public ResponseEntity<Oficio> find(
            @Parameter(description = "ID del oficio a buscar") @PathVariable Integer oid) {
        Oficio resultado = this.oficioService.findByOid(oid);
        if (resultado != null) {
            return ResponseEntity.ok(resultado);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(
        summary = "Guardar oficio",
        description = "Guarda un nuevo oficio en el sistema."
    )
    public ResponseEntity<?> save(
            @RequestBody(description = "Datos del oficio a registrar", required = true)
            @org.springframework.web.bind.annotation.RequestBody Oficio oficio) {
        try {
            Oficio resultado = oficioService.save(oficio);
            if (resultado != null) {
                return ResponseEntity.ok(resultado);
            }
            return ResponseEntity.internalServerError().body("Error: Resultado nulo");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{oid}")
    @Operation(
        summary = "Eliminar oficio",
        description = "Elimina un oficio del sistema por su identificador, si no tiene relaciones bloqueantes."
    )
    public ResponseEntity<String> delete(
            @Parameter(description = "ID del oficio a eliminar") @PathVariable Integer oid) {
        try {
            Oficio oficio = this.oficioService.findByOid(oid);
            if (oficio == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Oficio no encontrado");
            }

            this.oficioService.delete(oid);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede borrar por conflictos con otros datos");
        }
    }
}
