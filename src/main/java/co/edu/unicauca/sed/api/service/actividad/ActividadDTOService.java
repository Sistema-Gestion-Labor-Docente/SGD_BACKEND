package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.domain.Usuario;
import co.edu.unicauca.sed.api.dto.FuenteDTO;
import co.edu.unicauca.sed.api.dto.UsuarioDTO;
import co.edu.unicauca.sed.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sed.api.dto.actividad.ActividadDTOEvaluador;

/**
 * Interface para definir los métodos de conversión de entidades Actividad a DTOs.
 */
public interface ActividadDTOService {

    /**
     * Convierte una entidad Actividad en un DTO base.
     *
     * @param actividad La actividad a convertir.
     * @return DTO de la actividad.
     */
    ActividadBaseDTO buildActividadBaseDTO(Actividad actividad);

    /**
     * Convierte una actividad en un DTO incluyendo el evaluado y sus fuentes.
     *
     * @param actividad Actividad a convertir.
     * @return DTO de actividad con evaluado y fuentes.
     */
    ActividadDTOEvaluador convertToDTOWithEvaluado(Actividad actividad);

    /**
     * Convierte una actividad en un DTO incluyendo el evaluado y filtrando las fuentes.
     *
     * @param actividad   Actividad a convertir.
     * @param tipoFuente  Tipo de fuente para filtrar.
     * @param estadoFuente Estado de la fuente para filtrar.
     * @return DTO de actividad con evaluado y fuentes filtradas.
     */
    ActividadDTOEvaluador convertToDTOWithEvaluado(Actividad actividad, String tipoFuente, String estadoFuente);

    /**
     * Convierte una entidad Usuario en un DTO.
     *
     * @param usuario Usuario a convertir.
     * @return DTO del usuario.
     */
    UsuarioDTO convertToUsuarioDTO(Usuario usuario);

    /**
     * Convierte una entidad Fuente en un DTO.
     *
     * @param fuente Fuente a convertir.
     * @return DTO de la fuente.
     */
    FuenteDTO convertFuenteToDTO(Fuente fuente);
}
