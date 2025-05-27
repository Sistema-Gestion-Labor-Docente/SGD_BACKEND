package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.EstadoFuente;
import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.dto.FuenteCreateDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * Interfaz para manejar la lógica de negocio relacionada con fuentes.
 */
public interface FuenteBusinessService {

    /**
     * Procesa una fuente, actualizando o creando su información y archivos asociados.
     *
     * @param fuenteDTO            El DTO de la fuente.
     * @param informeFuente        Archivo común asociado a la fuente.
     * @param observacion          Observación general.
     * @param archivosEjecutivos   Archivos adicionales (informes ejecutivos).
     */
    void procesarFuente(FuenteCreateDTO fuenteDTO, MultipartFile informeFuente, String observacion, Map<String, MultipartFile> archivosEjecutivos);

    EstadoFuente determinarEstadoFuente(Fuente fuente);

    void actualizarFuente(Fuente fuente, float nuevaCalificacion, String tipoCalificacion, String observacion);}
