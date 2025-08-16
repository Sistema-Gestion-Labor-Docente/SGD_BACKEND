package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.NombreFecha;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTOResponse;

@Component
public class NombreFechaMapper {

    public NombreFecha toEntity(NombreFechaDTORequest dto) {
        NombreFecha e = new NombreFecha();
        e.setNombre(dto.getNombre());
        return e;
    }

    public void update(NombreFecha existente, NombreFechaDTORequest dto) {
        existente.setNombre(dto.getNombre());
    }

    public NombreFechaDTOResponse toResponse(NombreFecha e) {
        NombreFechaDTOResponse dto = new NombreFechaDTOResponse();
        dto.setOidNombreFecha(e.getOidNombreFecha());
        dto.setNombre(e.getNombre());
        dto.setFechaCreacion(e.getFechaCreacion());
        dto.setUsuarioCreacion(e.getUsuarioCreacion());
        dto.setFechaActualizacion(e.getFechaActualizacion());
        dto.setUsuarioActualizacion(e.getUsuarioActualizacion());
        return dto;
    }
}
