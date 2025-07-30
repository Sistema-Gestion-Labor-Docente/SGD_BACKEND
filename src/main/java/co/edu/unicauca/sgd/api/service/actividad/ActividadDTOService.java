package co.edu.unicauca.sgd.api.service.actividad;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;

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
     * Convierte una entidad Usuario en un DTO.
     *
     * @param usuario Usuario a convertir.
     * @return DTO del usuario.
     */
    UsuarioDTO convertToUsuarioDTO(Usuario usuario);
}
