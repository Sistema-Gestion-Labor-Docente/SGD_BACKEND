package co.edu.unicauca.sgd.api.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;

@Component
public class UsuarioActividadCalendarioMapper {

    // Convierte la entidad a DTOResponse
    public UsuarioActividadCalendarioDTOResponse toResponse(Actividad actividad, List<UsuarioActividadCalendario> relaciones, Calendario calendario, List<AtributoDTO> atributos) {
        UsuarioActividadCalendarioDTOResponse dto = new UsuarioActividadCalendarioDTOResponse();

        // Armar el DTO base con los campos que necesitas
        ActividadBaseDTO actividadDto = new ActividadBaseDTO();
        actividadDto.setOidActividad(actividad.getOidActividad());
        actividadDto.setTipoActividad(actividad.getTipoActividad());
        actividadDto.setOidEstadoActividad(actividad.getEstadoActividad().getOidEstadoActividad());
        actividadDto.setNombreActividad(actividad.getNombreActividad());
        actividadDto.setHoras(actividad.getHoras());
        actividadDto.setSemanas(actividad.getSemanas());
        actividadDto.setFechaCreacion(actividad.getFechaCreacion());
        actividadDto.setFechaActualizacion(actividad.getFechaActualizacion());
        actividadDto.setAtributos(atributos);

        dto.setActividad(actividadDto);

        // Setear calendario
        dto.setOidCalendario(calendario != null ? calendario.getOidcalendario() : null);
        dto.setNombreCalendario(calendario != null ? calendario.getAnioCalendario() + " - " + calendario.getNumeroCalendario() : null);

        // Usuarios relacionados
        List<UsuarioDTO> usuarios = relaciones.stream()
            .map(rel -> {
                Usuario usuario = rel.getUsuario();
                UsuarioDTO usuarioDto = new UsuarioDTO();
                usuarioDto.setOidUsuario(usuario.getOidUsuario());
                usuarioDto.setIdentificacion(usuario.getIdentificacion());
                usuarioDto.setNombres(usuario.getNombres());
                usuarioDto.setApellidos(usuario.getApellidos());
                return usuarioDto;
            })
            .toList();
        dto.setUsuarios(usuarios);

        return dto;
    }
}


