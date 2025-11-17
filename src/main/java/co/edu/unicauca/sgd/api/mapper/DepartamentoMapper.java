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
        d.setFacultad(dto.getFacultad());
        d.setCodigoKira(dto.getCodigoKira());
        return d;
    }

    public void actualizarCamposBasicos(Departamento existente, DepartamentoDTORequest dto) {
        existente.setNombre(dto.getNombre());
        existente.setFacultad(dto.getFacultad());
        existente.setCodigoKira(dto.getCodigoKira());
        // jefe lo actualiza el service
    }

    public DepartamentoDTOResponse toResponse(Departamento entidad) {
        DepartamentoDTOResponse dto = new DepartamentoDTOResponse();
        dto.setOidDepartamento(entidad.getOidDepartamento());
        dto.setNombre(entidad.getNombre());
        dto.setFacultad(entidad.getFacultad());
        dto.setCodigoKira(entidad.getCodigoKira());

        if (entidad.getJefe() != null) {
            dto.setJefeOidUsuario(entidad.getJefe().getOidUsuario());
            try {
                String nombres = entidad.getJefe().getNombres();
                String apellidos = entidad.getJefe().getApellidos();
                dto.setJefeNombre(
                    (nombres != null ? nombres : "") +
                    (apellidos != null ? " " + apellidos : "")
                );
            } catch (Exception ignored) { /* campos opcionales */ }
        }

        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setUsuarioCreacion(entidad.getUsuarioCreacion());
        dto.setFechaActualizacion(entidad.getFechaActualizacion());
        dto.setUsuarioActualizacion(entidad.getUsuarioActualizacion());
        return dto;
    }
}
