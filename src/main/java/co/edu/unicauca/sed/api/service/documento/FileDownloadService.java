package co.edu.unicauca.sed.api.service.documento;

import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileDownloadService {

    @Value("${DOCUMENT_UPLOAD_DIR}")
    private String documentUploadDir;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public InputStream createZipStream(String periodo, boolean esConsolidado, String departamento, String tipoContrato, Integer oidUsuario) throws IOException {
        Path basePath = construirRutaBase(periodo, esConsolidado, departamento, tipoContrato);
    
        if (esConsolidado && oidUsuario != null) {
            return obtenerArchivoConsolidado(basePath, periodo, oidUsuario);
        }

        if (oidUsuario != null) {
            String nombreUsuario;
            Usuario usuario = getUsuarioById(oidUsuario);
            nombreUsuario = formatUsuarioFolder(usuario);
            basePath = basePath.resolve(nombreUsuario);
        }
    
        if (!Files.exists(basePath)) {
            throw new FileNotFoundException("No se encontró la ruta: " + basePath);
        }
    
        return generarZip(basePath);
    }
    
    /**
     * Construye la ruta base según los parámetros recibidos.
     */
    private Path construirRutaBase(String periodo, boolean esConsolidado, String departamento, String tipoContrato) {
        Path basePath = Paths.get(documentUploadDir);
    
        if (periodo != null) {
            basePath = basePath.resolve(periodo);
        }
        if (esConsolidado) {
            basePath = basePath.resolve("Consolidados");
        }
        if (departamento != null) {
            basePath = basePath.resolve(departamento);
        }
        if (tipoContrato != null) {
            basePath = basePath.resolve(tipoContrato);
        }
    
        return basePath;
    }
    
    /**
     * Obtiene el archivo de consolidado específico para un usuario.
     */
    private InputStream obtenerArchivoConsolidado(Path basePath, String periodo, Integer oidUsuario) throws IOException {
        Usuario usuario = getUsuarioById(oidUsuario);
        String nombreArchivo = "Consolidado-" + periodo + "-" + formatUsuarioFolder(usuario) + ".xlsx";
        Path archivoConsolidado = basePath.resolve(nombreArchivo);
    
        if (!Files.exists(archivoConsolidado)) {
            throw new FileNotFoundException("No se encontró el archivo de consolidado: " + archivoConsolidado);
        }
    
        return Files.newInputStream(archivoConsolidado);
    }
    
    /**
     * Genera un archivo ZIP a partir del contenido de una carpeta.
     */
    private InputStream generarZip(Path directorioBase) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            try (Stream<Path> paths = Files.walk(directorioBase)) {
                paths.forEach(path -> agregarArchivoAZip(zos, directorioBase, path));
            }
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }
    
    /**
     * Agrega un archivo o carpeta al ZIP.
     */
    private void agregarArchivoAZip(ZipOutputStream zos, Path directorioBase, Path path) {
        try {
            Path relativePath = directorioBase.relativize(path);
            ZipEntry zipEntry = new ZipEntry(relativePath.toString() + (Files.isDirectory(path) ? "/" : ""));
            zos.putNextEntry(zipEntry);
    
            if (!Files.isDirectory(path)) {
                Files.copy(path, zos);
            }
            zos.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }       

    private Usuario getUsuarioById(Integer oidUsuario) {
        return usuarioRepository.findById(oidUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario con ID " + oidUsuario + " no encontrado"));
    }

    private String formatUsuarioFolder(Usuario usuario) {
        return usuario.getNombres().toUpperCase().replace(" ", "_") + "_" + usuario.getApellidos().toUpperCase().replace(" ", "_");
    }
}
