package co.edu.unicauca.sed.api.utils;

import org.springframework.web.multipart.MultipartFile;

import co.edu.unicauca.sed.api.dto.OdsDTO;

import java.nio.file.Paths;
import java.util.*;

public class ArchivoUtils {

    public static String extraerNombreArchivo(String rutaCompleta) {
        if (rutaCompleta == null || rutaCompleta.isBlank()) return null;
        return Paths.get(rutaCompleta).getFileName().toString();
    }

    public static Map<Integer, MultipartFile> mapearArchivosOdsPorNombre(List<OdsDTO> odsSeleccionados, Map<String, MultipartFile> archivos) {
        if (archivos == null || archivos.isEmpty() || odsSeleccionados == null || odsSeleccionados.isEmpty()) {
            return Map.of();
        }

        Map<Integer, MultipartFile> resultado = new HashMap<>();

        for (OdsDTO ods : odsSeleccionados) {
            String nombreDocumento = ods.getDocumento();
            Integer oidOds = ods.getOidOds();

            if (nombreDocumento != null && oidOds != null) {
                Optional<Map.Entry<String, MultipartFile>> archivoEncontrado = archivos.entrySet().stream()
                        .filter(entry -> entry.getValue().getOriginalFilename() != null &&
                                entry.getValue().getOriginalFilename().equals(nombreDocumento))
                        .findFirst();

                archivoEncontrado.ifPresent(entry -> resultado.put(oidOds, entry.getValue()));
            }
        }

        return resultado;
    }
}
