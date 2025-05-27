package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.service.fuente.FuenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("api/fuente")
@Tag(name = "Fuente", description = "Gestión de fuentes y documentos asociados")
public class FuenteController {

    private static final Logger logger = LoggerFactory.getLogger(FuenteController.class);

    @Autowired
    private FuenteService fuenteService;

    @GetMapping
    @Operation(summary = "Listar fuentes", description = "Obtiene todas las fuentes registradas con paginación")
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Fuente> fuentes = fuenteService.obtenerTodos(PageRequest.of(page, size));
            if (fuentes.hasContent()) {
                return ResponseEntity.ok().body(fuentes);
            } else {
                logger.warn("No se encontraron fuentes");
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            logger.error("Error al obtener las fuentes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{oid}")
    @Operation(summary = "Buscar fuente por ID", description = "Consulta una fuente específica por su identificador")
    public ResponseEntity<?> find(
            @Parameter(description = "ID de la fuente") @PathVariable Integer oid) {
        Fuente resultado = fuenteService.buscarPorId(oid);
        if (resultado != null) {
            return ResponseEntity.ok().body(resultado);
        }
        logger.warn("Fuente con ID {} no encontrada", oid);
        return ResponseEntity.notFound().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Guardar fuente con archivo", description = "Guarda una fuente junto con un archivo adjunto e información adicional")
    public ResponseEntity<?> saveFuente(
            @RequestParam("informeFuente") MultipartFile informeFuente,
            @RequestParam("observation") String observation,
            @RequestParam(required = false) String tipoCalificacion,
            @RequestParam("sources") String sourcesJson,
            @RequestParam(required = false) Map<String, MultipartFile> allFiles) {
        try {
            if (allFiles != null) {
                allFiles.forEach((key, file) -> logger.debug("   ➝ Archivo '{}' con tamaño {} bytes",
                    file.getOriginalFilename(), file.getSize()));
            } else {
                logger.debug("📌 Parámetro [allFiles]: No se recibieron archivos adicionales.");
            }
            fuenteService.guardarFuente(sourcesJson, informeFuente, observation, allFiles);
            return ResponseEntity.ok("Archivos procesados correctamente");
        } catch (Exception e) {
            logger.debug("Error al procesar los archivos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error procesando los archivos");
        }
    }

    @DeleteMapping("/{oid}")
    @Operation(summary = "Eliminar fuente", description = "Elimina una fuente por su ID")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID de la fuente a eliminar") @PathVariable Integer oid) {
        logger.info("Solicitud recibida para eliminar la fuente con ID: {}", oid);
        Fuente fuente = null;
        try {
            fuente = fuenteService.buscarPorId(oid);
            if (fuente == null) {
                logger.warn("Fuente con ID {} no encontrada", oid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fuente no encontrada");
            }
        } catch (Exception e) {
            logger.error("Error al buscar la fuente con ID {}: {}", oid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fuente no encontrada");
        }

        try {
            fuenteService.eliminar(oid);
            logger.info("Fuente con ID {} eliminada exitosamente", oid);
        } catch (Exception e) {
            logger.error("Error al eliminar la fuente con ID {}: {}", oid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede borrar por conflictos con otros datos");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "Descargar archivo de fuente", description = "Descarga el archivo asociado a una fuente, ya sea el informe o el documento fuente")
    public ResponseEntity<?> downloadFile(
            @Parameter(description = "ID de la fuente") @PathVariable("id") Integer id,
            @Parameter(description = "Indica si se debe descargar el informe (true) o el documento fuente (false)")
            @RequestParam(name = "report", defaultValue = "false") boolean isReport) {
        return fuenteService.obtenerArchivo(id, isReport);
    }
}