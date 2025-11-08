package co.edu.unicauca.sgd.api.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Actividad;
import co.edu.unicauca.sgd.api.domain.ActividadCalendario;
import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.CargoActividad;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import co.edu.unicauca.sgd.api.dto.RolDTO;
import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.UsuarioDetalleDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.DocenciaDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;

@Component
public class UsuarioActividadCalendarioMapper {

    private UsuarioDepartamentoRepository usuarioDepartamentoRepository;

    public UsuarioActividadCalendarioMapper(UsuarioDepartamentoRepository usuarioDepartamentoRepository) {
        this.usuarioDepartamentoRepository = usuarioDepartamentoRepository;
    }

    public UsuarioActividadCalendarioDTOResponse toResponse(Actividad actividad,
                                                           List<UsuarioActividadCalendario> relaciones,
                                                           Calendario calendario,
                                                           List<AtributoDTO> atributos) {
        UsuarioActividadCalendarioDTOResponse dto = new UsuarioActividadCalendarioDTOResponse();

        // Actividad base
        ActividadBaseDTO actividadDto = new ActividadBaseDTO();
        actividadDto.setOidActividad(actividad.getOidActividad());
        actividadDto.setTipoActividad(actividad.getTipoActividad());

        // CargoActividad: tomar de la primera relación si existe
        if (!relaciones.isEmpty() && relaciones.get(0).getActividadCalendario().getCargoActividad() != null) {
            CargoActividad ca = relaciones.get(0).getActividadCalendario().getCargoActividad();
            CargoActividadDTOResponse cargoDto = new CargoActividadDTOResponse();
            cargoDto.setOidCargoActividad(ca.getOidCargoActividad());
            cargoDto.setNombre(ca.getNombre());
            cargoDto.setMaxHorasSemana(ca.getMaxHorasSemana());
            actividadDto.setCargoActividad(cargoDto);
        } else {
            actividadDto.setCargoActividad(null);
        }

        actividadDto.setOidEstadoActividad(actividad.getEstadoActividad() != null ? actividad.getEstadoActividad().getOidEstadoActividad() : null);
        actividadDto.setNombreActividad(actividad.getNombreActividad());
        actividadDto.setHoras(actividad.getHoras());
        actividadDto.setSemanas(actividad.getSemanas());
        actividadDto.setFechaCreacion(actividad.getFechaCreacion());
        actividadDto.setFechaActualizacion(actividad.getFechaActualizacion());
        actividadDto.setAtributos(atributos == null ? List.of() : atributos);

        dto.setActividad(actividadDto);

        // Calendario: si viene por parámetro úsalo, si no, intenta extraerlo de la primera relación
        Calendario cal = calendario;
        if (cal == null && !relaciones.isEmpty()) {
            ActividadCalendario ac = relaciones.get(0).getActividadCalendario();
            cal = ac != null ? ac.getCalendario() : null;
        }
        dto.setOidCalendario(cal != null ? cal.getOidcalendario() : null);
        dto.setNombreCalendario(cal != null ? cal.getAnioCalendario() + " - " + cal.getNumeroCalendario() : null);

        // Usuarios relacionados (mapear cada relacion -> UsuarioDTO)
        List<UsuarioDTO> usuarios = relaciones.stream()
            .map(rel -> {
                Usuario usuario = rel.getUsuario();
                UsuarioDTO usuarioDto = new UsuarioDTO();

                // Buscar departamento ofertado (si existe)
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
                usuarioDto.setRoles(usuario.getRoles() == null ? List.of() : usuario.getRoles().stream()
                    .map(rol -> {
                        RolDTO rolDto = new RolDTO();
                        rolDto.setNombre(rol.getNombre());
                        return rolDto;
                    })
                    .collect(Collectors.toList()));
                usuarioDto.setUsuarioDetalle(UsuarioDetalleDTO.fromEntity(usuario.getUsuarioDetalle()));
                return usuarioDto;
            })
            .collect(Collectors.toList());

        dto.setUsuarios(usuarios);

        return dto;
    }

    // Otros métodos de mapeo si es necesario
    // Docencia
    public DocenciaDTOResponse toDocenciaResponse(Actividad actividad,
                                                List<UsuarioActividadCalendario> relaciones,
                                                Calendario calendario,
                                                List<AtributoDTO> atributos) {
        DocenciaDTOResponse dto = new DocenciaDTOResponse();
        // Reusar parte común
        UsuarioActividadCalendarioDTOResponse base = toResponse(actividad, relaciones, calendario, atributos);
        dto.setActividad(base.getActividad());
        dto.setUsuarios(base.getUsuarios());
        dto.setOidCalendario(base.getOidCalendario());
        dto.setNombreCalendario(base.getNombreCalendario());

        // Campos específicos de Docencia (ejemplo)
        // Por ejemplo: cargaHorariaDocencia, programa, asignatura — extraer de atributos EAV si aplica
        dto.setCargaHorariaDocencia(extractAtributoFloat(atributos, "carga_horaria_docencia"));
        dto.setAsignatura(extractAtributoString(atributos, "asignatura"));

        return dto;
    }

    // Helpers para extraer atributos (puedes mover a utilidad propia)
    private String extractAtributoString(List<AtributoDTO> atributos, String nombre) {
        if (atributos == null) return null;
        return atributos.stream()
                .filter(a -> a.getCodigoAtributo().equalsIgnoreCase(nombre))
                .map(AtributoDTO::getValor)
                .findFirst()
                .orElse(null);
    }

    private Float extractAtributoFloat(List<AtributoDTO> atributos, String nombre) {
        String val = extractAtributoString(atributos, nombre);
        if (val == null) return null;
        try { return Float.valueOf(val); } catch (NumberFormatException e) { return null; }
    }

}


