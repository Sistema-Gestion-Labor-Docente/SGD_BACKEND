package co.edu.unicauca.sgd.api.service.fuente;

import java.util.List;

import co.edu.unicauca.sgd.api.domain.Fuente;
import co.edu.unicauca.sgd.api.dto.FuenteDTO;

/**
 * Interfaz para definir los métodos del servicio de conversión de Fuente a DTO.
 */
public interface FuenteDTOService {

    /**
     * Convierte una entidad Fuente en un FuenteDTO.
     *
     * @param fuente La entidad Fuente a convertir.
     * @return El objeto FuenteDTO resultante.
     */
    FuenteDTO convertirADTO(Fuente fuente);

    /**
     * Convierte una entidad Fuente en un FuenteDTO básico con campos específicos.
     *
     * @param fuente La entidad Fuente a convertir.
     * @return El objeto FuenteDTO básico resultante.
     */
    FuenteDTO convertirADTOBasico(Fuente fuente);

    /**
     * Convierte una lista de entidades Fuente en una lista de FuenteDTO.
     *
     * @param fuentes La lista de entidades Fuente a convertir.
     * @return La lista de objetos FuenteDTO resultantes.
     */
    List<FuenteDTO> convertirListaFuenteADTO(List<Fuente> fuentes);
}
