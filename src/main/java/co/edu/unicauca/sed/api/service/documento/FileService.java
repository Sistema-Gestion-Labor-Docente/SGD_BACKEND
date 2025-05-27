package co.edu.unicauca.sed.api.service.documento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    @Value("${document.upload-dir}")
    private String directorioSubida;

    public Path guardarArchivo(MultipartFile archivo, String periodoAcademico, String nombreEvaluado, String contratacion, String departamento) throws IOException {
        return guardarArchivo(archivo, periodoAcademico, nombreEvaluado, contratacion, departamento, null, null, null);
    }

    public Path guardarArchivo(MultipartFile archivo, String periodoAcademico, String nombreEvaluado, String contratacion, String departamento, String nombreActividad, String idEvaluador, String prefijo) throws IOException {

        String contratacionLimpia = limpiarNombre(contratacion);
        String actividad = limpiarNombre(nombreActividad);

        Path directorioPath = construirRutaDinamica(directorioSubida, periodoAcademico, departamento, contratacionLimpia, nombreEvaluado, actividad);
        Files.createDirectories(directorioPath);
        String nombreOriginal = archivo.getOriginalFilename();
        String segmentoEvaluador = (idEvaluador != null && !idEvaluador.isEmpty()) ? idEvaluador + "-" : "";
        String nombreConPrefijo = (prefijo != null && !prefijo.isEmpty() && !nombreOriginal.startsWith(prefijo + "-")) ? limpiarNombre(prefijo + "-" + segmentoEvaluador + nombreOriginal) : nombreOriginal;

        Path rutaDestino = directorioPath.resolve(nombreConPrefijo);

        try {
            Files.write(rutaDestino, archivo.getBytes());
        } catch (IOException e) {
            LOGGER.error("❌ Error al guardar el archivo: {}, Error: {}", rutaDestino, e.getMessage(), e);
            throw e;
        }

        return rutaDestino;
    }

    public void eliminarArchivo(String rutaArchivo) {
        try {
            if (rutaArchivo != null) {
                Path path = Paths.get(rutaArchivo);
                if (Files.exists(path)) {
                    Files.delete(path);
                    LOGGER.info("✅ Archivo eliminado exitosamente: {}", rutaArchivo);
                } else {
                    LOGGER.warn("⚠️ Intento de eliminar archivo inexistente: {}", rutaArchivo);
                }
            }
        } catch (IOException e) {
            LOGGER.error("❌ Error al eliminar el archivo: {}, Error: {}", rutaArchivo, e.getMessage(), e);
        }
    }

    public Resource obtenerRecursoArchivo(String rutaArchivo) throws Exception {
        Path pathArchivo = Paths.get(rutaArchivo);
        try {
            if (!Files.exists(pathArchivo) || !Files.isReadable(pathArchivo)) {
                LOGGER.warn("⚠️ El archivo no existe o no es accesible: {}", rutaArchivo);
                throw new RuntimeException("El archivo no existe o no es accesible: " + rutaArchivo);
            }
            return new UrlResource(pathArchivo.toUri());
        } catch (Exception e) {
            LOGGER.error("❌ Error al recuperar el archivo: {}, Error: {}", rutaArchivo, e.getMessage(), e);
            throw e;
        }
    }

    private Path construirRutaDinamica(String... segmentos) {
        List<String> segmentosValidos = Arrays.stream(segmentos)
                .filter(segmento -> segmento != null && !segmento.isEmpty())
                .collect(Collectors.toList());

        if (segmentosValidos.isEmpty()) {
            throw new IllegalArgumentException("La ruta no puede estar vacía ni compuesta solo de segmentos nulos o vacíos.");
        }

        return Paths.get(segmentosValidos.get(0), segmentosValidos.subList(1, segmentosValidos.size()).toArray(new String[0]));
    }

    private String limpiarNombre(String valor) {
        return Optional.ofNullable(valor)
            .orElse("")
            .replaceAll("[\\\\/:*?\"<>|]", "_")
            .replaceAll("\\s+", "_")
            .trim();
    }
}