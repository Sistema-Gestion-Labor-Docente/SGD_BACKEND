package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.dto.FuenteCreateDTO;
import co.edu.unicauca.sed.api.service.documento.FileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio para manejar archivos relacionados con fuentes.
 */
@Service
public class FuenteFileServiceImpl implements FuenteFileService {

    private static final Logger logger = LoggerFactory.getLogger(FuenteFileServiceImpl.class);

    @Autowired
    private FileService fileService;

    @Override
    public Path manejarArchivoFuente(Optional<Fuente> fuenteOpcional, MultipartFile informeFuente, String periodoAcademico, 
                                     String nombreEvaluado, String contratacion, String departamento, 
                                     String nombreActividad, String idEvaluador, String prefijo) {
        
        try {
            if (informeFuente == null || informeFuente.isEmpty()) {
                logger.warn("El archivo fuente no fue proporcionado.");
                return null;
            }

            // Si existe una fuente previa, verifica si el archivo es diferente
            if (fuenteOpcional.isPresent()) {
                Fuente fuenteExistente = fuenteOpcional.get();
                String nombreArchivoExistente = fuenteExistente.getRutaDocumentoFuente() != null
                        ? Path.of(fuenteExistente.getRutaDocumentoFuente()).getFileName().toString() : null;
                if (nombreArchivoExistente != null && !informeFuente.getOriginalFilename().equals(nombreArchivoExistente)) {
                    fileService.eliminarArchivo(fuenteExistente.getRutaDocumentoFuente());
                    logger.info("Archivo fuente existente eliminado: {}", nombreArchivoExistente);
                }
            }

            Path archivoGuardado = fileService.guardarArchivo(informeFuente, periodoAcademico, nombreEvaluado, contratacion, departamento, nombreActividad, idEvaluador, prefijo);
            return archivoGuardado;

        } catch (Exception e) {
            logger.error("Error al manejar el archivo fuente común", e);
            throw new RuntimeException("Error al manejar el archivo fuente común: " + e.getMessage(), e);
        }
    }

    @Override
    public Path manejarInformeEjecutivo(Optional<Fuente> fuenteOpcional, FuenteCreateDTO fuenteDTO,
                                        Map<String, MultipartFile> archivosEjecutivos, String periodoAcademico,
                                        String nombreEvaluado, String contratacion, String departamento) {
        try {
            // Si no hay informe ejecutivo en la nueva solicitud
            if (fuenteDTO.getInformeEjecutivo() == null || fuenteDTO.getInformeEjecutivo().isEmpty()) {
                if (fuenteOpcional.isPresent()) {
                    Fuente fuenteExistente = fuenteOpcional.get();
                    if (fuenteExistente.getRutaDocumentoInforme() != null) {
                        // Eliminar el archivo previo si existe
                        fileService.eliminarArchivo(fuenteExistente.getRutaDocumentoInforme());
                        logger.info("Informe ejecutivo previo eliminado: {}", fuenteExistente.getRutaDocumentoInforme());

                        // Actualizar la entidad Fuente para eliminar la referencia al archivo
                        fuenteExistente.setNombreDocumentoInforme(null);
                        fuenteExistente.setRutaDocumentoInforme(null);
                    }
                }
                return null; // No hay archivo nuevo que guardar
            }

            // Buscar el archivo correspondiente en los archivos adicionales
            Optional<MultipartFile> archivoCoincidente = archivosEjecutivos.values().stream()
                .filter(file -> file.getOriginalFilename().equalsIgnoreCase(fuenteDTO.getInformeEjecutivo())).findFirst();

            if (archivoCoincidente.isEmpty()) {
                logger.warn("No se encontró un archivo coincidente para el informe ejecutivo: {}", fuenteDTO.getInformeEjecutivo());
                return null;
            }

            if (fuenteOpcional.isPresent()) {
                Fuente fuenteExistente = fuenteOpcional.get();
                String nombreInformeExistente = fuenteExistente.getNombreDocumentoInforme();

                // Eliminar el archivo previo si es diferente
                if (nombreInformeExistente != null && !fuenteDTO.getInformeEjecutivo().equals(nombreInformeExistente)) {
                    fileService.eliminarArchivo(fuenteExistente.getRutaDocumentoInforme());
                    logger.info("Informe ejecutivo previo eliminado: {}", nombreInformeExistente);
                }
            }

            // Guardar el nuevo informe ejecutivo
            Path archivoGuardado = fileService.guardarArchivo(archivoCoincidente.get(), periodoAcademico,
                    nombreEvaluado, contratacion, departamento, null, null, "informe");
            return archivoGuardado;

        } catch (Exception e) {
            logger.error("Error al manejar el informe ejecutivo", e);
            throw new RuntimeException("Error al manejar el informe ejecutivo: " + e.getMessage(), e);
        }
    }
}
