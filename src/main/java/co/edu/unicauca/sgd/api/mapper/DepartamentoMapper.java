package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTOResponse;

@Component
public class DepartamentoMapper {

    public Departamento convertToEntity(DepartamentoDTORequest dto) {
        Departamento d = new Departamento();
        d.setNombre(dto.getNombre());
        return d;
    }

    public void actualizarCamposBasicos(Departamento existente, DepartamentoDTORequest dto) {
        existente.setNombre(dto.getNombre());
    }

    public DepartamentoDTOResponse toResponse(Departamento entidad) {
        DepartamentoDTOResponse dto = new DepartamentoDTOResponse();
        dto.setOidDepartamento(entidad.getOidDepartamento());
        dto.setNombre(entidad.getNombre());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setUsuarioCreacion(entidad.getUsuarioCreacion());
        dto.setFechaActualizacion(entidad.getFechaActualizacion());
        dto.setUsuarioActualizacion(entidad.getUsuarioActualizacion());
        return dto;
    }
}
