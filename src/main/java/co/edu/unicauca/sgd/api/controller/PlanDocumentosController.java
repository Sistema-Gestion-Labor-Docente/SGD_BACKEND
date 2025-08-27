package co.edu.unicauca.sgd.api.controller;

import java.io.ByteArrayOutputStream;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.sgd.api.service.materias.PlanDocumentosService;
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
        try {
            ByteArrayOutputStream bos = planDocumentosService.generarFormatoAdicion(oidPlan);
            byte[] contenido = bos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename("Formato_Adicion_" + oidPlan + ".xlsx").build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contenido);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
