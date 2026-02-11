package co.edu.unicauca.sgd.api.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.sgd.api.exception.necesidad.NecesidadValidationException;
import co.edu.unicauca.sgd.api.service.necesidad.NecesidadDocumentosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/necesidades/documentos")
@Tag(name = "Necesidad - Documentos", description = "Exportacion de necesidades en Excel")
public class NecesidadDocumentosController {

    private final NecesidadDocumentosService necesidadDocumentosService;

    public NecesidadDocumentosController(NecesidadDocumentosService necesidadDocumentosService) {
        this.necesidadDocumentosService = necesidadDocumentosService;
    }

    @GetMapping
    @Operation(summary = "Descargar necesidades en Excel",
            description = "Genera un Excel con las necesidades de un calendario y, opcionalmente, de un departamento.")
    public ResponseEntity<byte[]> descargarNecesidades(@RequestParam Integer oidCalendario,
                                                       @RequestParam(required = false) Integer oidDepartamento) {
        if (oidCalendario == null || oidCalendario <= 0) {
            throw new NecesidadValidationException("El oidCalendario es obligatorio y debe ser mayor que cero.");
        }
        if (oidDepartamento != null && oidDepartamento <= 0) {
            throw new NecesidadValidationException("El oidDepartamento debe ser mayor que cero.");
        }

        try {
            ByteArrayOutputStream bos = necesidadDocumentosService
                    .generarFormatoNecesidades(oidCalendario, oidDepartamento);
            byte[] contenido = bos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("Necesidades_" + oidCalendario + ".xlsx").build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contenido);
        } catch (IOException e) {
            throw new RuntimeException("Error generando el formato de necesidades.", e);
        }
    }
}
