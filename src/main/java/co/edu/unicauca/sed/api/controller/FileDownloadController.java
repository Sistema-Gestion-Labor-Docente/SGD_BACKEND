package co.edu.unicauca.sed.api.controller;

import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.service.documento.FileDownloadService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("api/download")
public class FileDownloadController {

    private final FileDownloadService fileDownloadService;

    public FileDownloadController(FileDownloadService fileDownloadService) {
        this.fileDownloadService = fileDownloadService;
    }

    @GetMapping("/zip")
    public ResponseEntity<?> downloadZip(
            @RequestParam(value = "periodo", required = false) String periodo,
            @RequestParam(value = "departamento", required = false) String departamento,
            @RequestParam(value = "tipoContrato", required = false) String tipoContrato,
            @RequestParam(value = "oidUsuario", required = false) Integer oidUsuario,
            @RequestParam(defaultValue = "false") boolean esConsolidado) {

        if (departamento != null && periodo == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Si se envía 'departamento', también se debe enviar 'periodo'.",null
            ));
        }
        if (tipoContrato != null && (departamento == null || periodo == null)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400,
                "Si se envía 'tipoContrato', también se deben enviar 'periodo' y 'departamento'.", null
            ));
        }
        if (oidUsuario != null && (tipoContrato == null || departamento == null || periodo == null)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400,
                "Si se envía 'oidUsuario', también se deben enviar 'periodo', 'departamento' y 'tipoContrato'.", null
            ));
        }

        try {
            InputStream fileStream = fileDownloadService.createZipStream(periodo, esConsolidado, departamento, tipoContrato, oidUsuario);
            InputStreamResource resource = new InputStreamResource(fileStream);

            HttpHeaders headers = new HttpHeaders();
            String filename = definirNombreArchivo(periodo, esConsolidado, oidUsuario);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
        } catch (FileNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Archivo o directorio no encontrado: " + ex.getMessage(), null));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(500, "Error al generar el archivo: " + ex.getMessage(), null));
        }
    }

    /**
     * Define el nombre del archivo de salida según el tipo de descarga.
     */
    private String definirNombreArchivo(String periodo, boolean esConsolidado, Integer oidUsuario) {
        if (esConsolidado && oidUsuario != null) {
            return "Consolidado-" + periodo + ".xlsx";
        }
        return esConsolidado ? "Consolidados-" + periodo + ".zip" : "descarga_" + (periodo != null ? periodo : "all") + ".zip";
    }
}
