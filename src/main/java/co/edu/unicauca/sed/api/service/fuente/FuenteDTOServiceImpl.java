package co.edu.unicauca.sed.api.service.fuente;

import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.dto.FuenteDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para la conversión de entidades Fuente a DTO.
 */
@Service
public class FuenteDTOServiceImpl implements FuenteDTOService {

    @Override
    public FuenteDTO convertirADTO(Fuente fuente) {
        if (fuente == null) {
            return null;
        }

        return new FuenteDTO(
                fuente.getOidFuente(),
                fuente.getTipoFuente(),
                fuente.getCalificacion(),
                fuente.getTipoCalificacion(),
                fuente.getNombreDocumentoFuente(),
                fuente.getNombreDocumentoInforme(),
                fuente.getObservacion(),
                fuente.getFechaCreacion(),
                fuente.getFechaActualizacion(),
                fuente.getEstadoFuente() != null ? fuente.getEstadoFuente().getNombreEstado() : null
        );
    }

    @Override
    public FuenteDTO convertirADTOBasico(Fuente fuente) {
        if (fuente == null) {
            return null;
        }

        return new FuenteDTO(
                fuente.getOidFuente(),
                fuente.getEstadoFuente() != null ? fuente.getEstadoFuente().getNombreEstado() : null,
                fuente.getCalificacion(),
                fuente.getTipoFuente()
        );
    }

    @Override
    public List<FuenteDTO> convertirListaFuenteADTO(List<Fuente> fuentes) {
        if (fuentes == null || fuentes.isEmpty()) {
            return new ArrayList<>();
        }
    
        return fuentes.stream()
                .sorted(Comparator.comparing(Fuente::getTipoFuente))
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }    
}
