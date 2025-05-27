package co.edu.unicauca.sed.api.service.documento;

import co.edu.unicauca.sed.api.dto.ArchivoDTO;
import co.edu.unicauca.sed.api.service.evaluacion_docente.AutoevaluacionOdsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentoService.class);
    private final FileService fileService;
    private final AutoevaluacionOdsService autoevaluacionOdsService;

    public ResponseEntity<?> obtenerArchivoPorTipo(Integer idArchivo, String tipoArchivo) {
        try {
            String rutaArchivo = null;
            String nombreArchivo = null;

            switch (tipoArchivo.toUpperCase()) {
                case "ODS":
                    ArchivoDTO archivoODS = autoevaluacionOdsService.obtenerArchivoPorId(idArchivo);
                    if (archivoODS == null) {
                        throw new RuntimeException("ODS con ID " + idArchivo + " no encontrado.");
                    }
                    rutaArchivo = archivoODS.getRuta();
                    nombreArchivo = archivoODS.getNombre();
                    break;

                default: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo de archivo no soportado: " + tipoArchivo);
            }

            if (rutaArchivo == null || rutaArchivo.isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El archivo solicitado no está disponible para este tipo de documento.");
            }

            Resource recurso = fileService.obtenerRecursoArchivo(rutaArchivo);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"").body(recurso);

        } catch (Exception e) {
            LOGGER.error("❌ Error al obtener el archivo de tipo {} con ID {}: {}", tipoArchivo, idArchivo,e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Error al obtener el archivo: " + e.getMessage());
        }
    }
}
