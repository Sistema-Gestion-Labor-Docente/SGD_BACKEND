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

import co.edu.unicauca.sgd.api.dto.reportes.ReporteEstadisticasPdfSeleccionRequest;
import co.edu.unicauca.sgd.api.service.reportes.EstadisticasPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/estadisticas")
@Tag(name = "Estadisticas", description = "Exportacion de reportes de estadisticas")
public class EstadisticasController {

    private final EstadisticasPdfService estadisticasPdfService;

    public EstadisticasController(EstadisticasPdfService estadisticasPdfService) {
        this.estadisticasPdfService = estadisticasPdfService;
    }

    @PostMapping("/pdf")
    @Operation(summary = "Exportar reporte de estadisticas en PDF",
            description = "Genera un PDF con titulo, resumen y graficos a partir de la plantilla Formato_EstadisticasPDF.html")
    public ResponseEntity<byte[]> exportarPdf(@Valid @RequestBody ReporteEstadisticasPdfSeleccionRequest request) {
        try {
            ByteArrayOutputStream pdfStream = estadisticasPdfService.generarReportePdf(request);
            byte[] contenido = pdfStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("Reporte_Estadisticas.pdf").build());

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
