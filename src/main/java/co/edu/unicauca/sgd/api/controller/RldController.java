package co.edu.unicauca.sgd.api.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.sgd.api.dto.reportes.RldPdfRequest;
import co.edu.unicauca.sgd.api.service.reportes.RldPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/rld")
@Tag(name = "RLD", description = "Exportacion del Resumen Labor Docente")
public class RldController {

    private final RldPdfService rldPdfService;

    public RldController(RldPdfService rldPdfService) {
        this.rldPdfService = rldPdfService;
    }

    @PostMapping("/pdf")
    @Operation(summary = "Exportar RLD en PDF",
            description = "Genera el Resumen de Labor Docente en PDF con base en un docente y un calendario.")
    public ResponseEntity<byte[]> exportarRld(@Valid @RequestBody RldPdfRequest request) {
        try {
            ByteArrayOutputStream pdfStream = rldPdfService.generarRldPdf(request);
            byte[] contenido = pdfStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("RLD_" + request.getOidDocente() + "_" + request.getOidCalendario() + ".pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contenido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
