package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.dto.UsuarioDepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.UsuarioDepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.domain.Departamento;

@Component
public class UsuarioDepartamentoMapper {

    public UsuarioDepartamento convertToEntity(UsuarioDepartamentoDTORequest dto) {
        UsuarioDepartamento ud = new UsuarioDepartamento();
        ud.setOidUsuario(dto.getOidUsuario());
        ud.setDepartamento(new Departamento(dto.getOidDepartamento()));
        // Nota: no es necesario setear Usuario; si tu entidad lo expone, podrías:
        // ud.setUsuario(new Usuario(dto.getOidUsuario()));
        return ud;
    }

    public void actualizarCamposBasicos(UsuarioDepartamento existente, UsuarioDepartamentoDTORequest dto) {
        // Reasigna el departamento del usuario
        if (existente.getDepartamento() == null
            || !existente.getDepartamento().getOidDepartamento().equals(dto.getOidDepartamento())) {
            existente.setDepartamento(new Departamento(dto.getOidDepartamento()));
        }
        // El OIDUSUARIO es PK; NO lo tocamos en update.
    }

    public UsuarioDepartamentoDTOResponse toResponse(UsuarioDepartamento entidad) {
        UsuarioDepartamentoDTOResponse dto = new UsuarioDepartamentoDTOResponse();
        dto.setOidUsuario(entidad.getOidUsuario());
        if (entidad.getDepartamento() != null) {
            dto.setOidDepartamento(entidad.getDepartamento().getOidDepartamento());
            dto.setNombreDepartamento(entidad.getDepartamento().getNombre());
        }
        dto.setFechaCreacion(entidad.getFechaCreacion());
        return dto;
    }
}
