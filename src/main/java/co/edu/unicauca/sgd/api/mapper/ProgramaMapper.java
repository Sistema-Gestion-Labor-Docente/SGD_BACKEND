package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Programa;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTOResponse;

@Component
public class ProgramaMapper {

    public Programa convertToEntity(ProgramaDTORequest dto) {
        Programa programa = new Programa();
        programa.setNombre(dto.getNombre());
        return programa;
    }

    public void actualizarCamposBasicos(Programa existente, ProgramaDTORequest dto) {
        existente.setNombre(dto.getNombre());
    }

    public ProgramaDTOResponse toResponse(Programa entidad) {
        ProgramaDTOResponse dto = new ProgramaDTOResponse();
        dto.setOidPrograma(entidad.getOidPrograma());
        dto.setNombre(entidad.getNombre());

        if (entidad.getCoordinador() != null) {
            dto.setCoordinadorOidUsuario(entidad.getCoordinador().getOidUsuario());
            try {
                String nombres = entidad.getCoordinador().getNombres();
                String apellidos = entidad.getCoordinador().getApellidos();
                dto.setCoordinadorNombre(
                    (nombres != null ? nombres : "") +
                    (apellidos != null ? " " + apellidos : "")
                );
            } catch (Exception ignored) { 
                dto.setCoordinadorNombre("");
            }
        }

        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setUsuarioCreacion(entidad.getUsuarioCreacion());
        dto.setFechaActualizacion(entidad.getFechaActualizacion());
        dto.setUsuarioActualizacion(entidad.getUsuarioActualizacion());
        return dto;
    }
}
