package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.dto.FuenteCreateDTO;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * Interfaz para el manejo de archivos relacionados con fuentes.
 */
public interface FuenteFileService {

    /**
     * Maneja el archivo fuente común, eliminándolo y guardándolo si es necesario.
     *
     * @param fuenteOpcional     Fuente existente opcional.
     * @param informeFuente      Nuevo archivo fuente.
     * @param periodoAcademico   Identificador del período académico.
     * @param nombreEvaluado     Nombre del evaluado.
     * @param contratacion       Tipo de contratación.
     * @param departamento       Departamento al que pertenece.
     * @param nombreActividad    Nombre de la actividad.
     * @param idEvaluador        Identificador del evaluador.
     * @return La ruta del archivo guardado.
     */
    Path manejarArchivoFuente(Optional<Fuente> fuenteOpcional, MultipartFile informeFuente, String periodoAcademico, 
                              String nombreEvaluado, String contratacion, String departamento, 
                              String nombreActividad, String idEvaluador, String prefijo);

    /**
     * Maneja el informe ejecutivo de una fuente, eliminando el anterior si es necesario.
     *
     * @param fuenteOpcional     Fuente existente opcional.
     * @param fuenteDTO          DTO con información de la fuente.
     * @param archivosEjecutivos Archivos cargados en la solicitud.
     * @param periodoAcademico   Identificador del período académico.
     * @param nombreEvaluado     Nombre del evaluado.
     * @param contratacion       Tipo de contratación.
     * @param departamento       Departamento al que pertenece.
     * @return La ruta del informe ejecutivo guardado.
     */
    Path manejarInformeEjecutivo(Optional<Fuente> fuenteOpcional, FuenteCreateDTO fuenteDTO, 
                                 Map<String, MultipartFile> archivosEjecutivos, String periodoAcademico, 
                                 String nombreEvaluado, String contratacion, String departamento);
}
