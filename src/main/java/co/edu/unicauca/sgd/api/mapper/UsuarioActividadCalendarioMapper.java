package co.edu.unicauca.sgd.api.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.RolDTO;
import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;

@Component
public class UsuarioActividadCalendarioMapper {

    private UsuarioDepartamentoRepository usuarioDepartamentoRepository;

    public UsuarioActividadCalendarioMapper(UsuarioDepartamentoRepository usuarioDepartamentoRepository) {
        this.usuarioDepartamentoRepository = usuarioDepartamentoRepository;
    }
    // Convierte la entidad a DTOResponse
    public UsuarioActividadCalendarioDTOResponse toResponse(Actividad actividad, List<UsuarioActividadCalendario> relaciones, Calendario calendario, List<AtributoDTO> atributos) {
        UsuarioActividadCalendarioDTOResponse dto = new UsuarioActividadCalendarioDTOResponse();

        // Armar el DTO base con los campos que necesitas
        ActividadBaseDTO actividadDto = new ActividadBaseDTO();
        actividadDto.setOidActividad(actividad.getOidActividad());
        actividadDto.setTipoActividad(actividad.getTipoActividad());

        if (relaciones.get(0).getCargoActividad() != null) {
            CargoActividadDTOResponse cargoDto = new CargoActividadDTOResponse();
            cargoDto.setOidCargoActividad(relaciones.get(0).getCargoActividad().getOidCargoActividad());
            cargoDto.setNombre(relaciones.get(0).getCargoActividad().getNombre());
            cargoDto.setMaxHorasSemana(relaciones.get(0).getCargoActividad().getMaxHorasSemana());
            actividadDto.setCargoActividad(cargoDto);
        } else {
            actividadDto.setCargoActividad(null);
        }

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

                UsuarioDepartamento usuarioDepartamento = usuarioDepartamentoRepository.findById(usuario.getOidUsuario()).orElse(null);

                if (usuarioDepartamento != null) {
                    DepartamentoDTOResponse departamentoDto = new DepartamentoDTOResponse();
                    departamentoDto.setOidDepartamento(usuarioDepartamento.getDepartamento().getOidDepartamento());
                    departamentoDto.setNombre(usuarioDepartamento.getDepartamento().getNombre());
                    departamentoDto.setFacultad(usuarioDepartamento.getDepartamento().getFacultad());

                    usuarioDto.setDepartamento(departamentoDto);
                }

                usuarioDto.setOidUsuario(usuario.getOidUsuario());
                usuarioDto.setIdentificacion(usuario.getIdentificacion());
                usuarioDto.setNombres(usuario.getNombres());
                usuarioDto.setApellidos(usuario.getApellidos());
                usuarioDto.setRoles(usuario.getRoles().stream()
                    .map(rol -> {
                        RolDTO rolDto = new RolDTO();
                        rolDto.setNombre(rol.getNombre());
                        return rolDto;
                    })
                    .toList());
                return usuarioDto;
            })
            .toList();
        dto.setUsuarios(usuarios);

        return dto;
    }
}


