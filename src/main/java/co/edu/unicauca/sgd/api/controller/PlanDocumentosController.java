package co.edu.unicauca.sgd.api.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unicauca.sgd.api.exception.materias.PlanDocumentoValidationException;
import co.edu.unicauca.sgd.api.service.materias.PlanDocumentosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/planes/documentos")
@Tag(name = "Documentos", description = "Gestión de documentos de planes de estudio")
public class PlanDocumentosController {

    private PlanDocumentosService planDocumentosService;

    public PlanDocumentosController(PlanDocumentosService planDocumentosService) {
        this.planDocumentosService = planDocumentosService;
    }

    @GetMapping("/")
    public ResponseEntity<byte[]> descargarFormatoAdicion(@RequestParam Integer oidPlan) {
        if (oidPlan == null || oidPlan <= 0) {
            throw new PlanDocumentoValidationException("El oidPlan es obligatorio y debe ser mayor que cero.");
        }

        try {
            ByteArrayOutputStream bos = planDocumentosService.generarFormatoAdicion(oidPlan);
            byte[] contenido = bos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("Formato_Adicion_" + oidPlan + ".xlsx").build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contenido);
        } catch (IOException e) {
            // Se deja que GlobalExceptionHandler maneje el error general como 500.
            throw new RuntimeException("Error generando el formato de adición.", e);
        }
    }

    @PostMapping("/")
    @Operation(summary = "Carga de materias por archivo Excel",
            description = "Carga todas las materias de un plan de estudio desde el formato Excel de adición.")
    public ResponseEntity<?> cargarMateriasDesdeExcel(
            @RequestParam("oidPlan") Integer oidPlan,
            @RequestParam("file") MultipartFile file,
            Principal principal // <-- asume Spring Security, sino pásalo de otra forma
    ) {
        if (oidPlan == null || oidPlan <= 0) {
            throw new PlanDocumentoValidationException("El oidPlan es obligatorio y debe ser mayor que cero.");
        }
        if (file == null || file.isEmpty()) {
            throw new PlanDocumentoValidationException("El archivo es obligatorio y no puede estar vacío.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                && !contentType.equalsIgnoreCase("application/octet-stream")) {
            throw new PlanDocumentoValidationException("El archivo debe ser un Excel en formato .xlsx.");
        }

        try (InputStream excel = file.getInputStream()) {
            planDocumentosService.cargarMateriasDesdeExcel(excel, oidPlan);
            return ResponseEntity.ok("Materias cargadas correctamente.");
        } catch (IOException e) {
            throw new RuntimeException("Error procesando el archivo de materias.", e);
        }
    }

}
