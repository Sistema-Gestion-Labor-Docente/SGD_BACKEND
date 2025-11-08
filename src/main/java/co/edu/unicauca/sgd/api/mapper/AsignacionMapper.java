package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Asignacion;
import co.edu.unicauca.sgd.api.domain.Materia;
import co.edu.unicauca.sgd.api.domain.Necesidad;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTOResponse;

@Component
public class AsignacionMapper {

    public AsignacionDTOResponse toResponse(Asignacion asignacion) {
        Seleccionado seleccionado = asignacion.getSeleccionado();
        Necesidad necesidad = asignacion.getNecesidad();
        Materia materia = necesidad != null ? necesidad.getMateria() : null;
        Usuario usuario = seleccionado != null ? seleccionado.getUsuario() : null;

        return AsignacionDTOResponse.builder()
                .oidAsignacion(asignacion.getOidAsignacion())
                .oidNecesidad(necesidad != null ? necesidad.getOidNecesidad() : null)
                .oidSeleccionado(seleccionado != null ? seleccionado.getOidSeleccionado() : null)
                .oidActividad(asignacion.getActividad() != null ? asignacion.getActividad().getOidActividad() : null)
                .horasDocencia(asignacion.getHorasDocencia())
                .semanasDocencia(asignacion.getSemanasDocencia())
                .horasPreparacion(asignacion.getHorasPreparacion())
                .semanasPreparacion(asignacion.getSemanasPreparacion())
                .nombreActividad(asignacion.getActividad() != null ? asignacion.getActividad().getNombreActividad() : null)
                .nombreDocente(usuario != null ? usuario.getNombres() + " " + usuario.getApellidos() : null)
                .codigoMateria(materia != null ? materia.getCodigo() : null)
                .nombreMateria(materia != null ? materia.getNombre() : null)
                .grupo(necesidad != null ? necesidad.getGrupo() : null)
                .numeroCalendario(necesidad != null && necesidad.getCalendario() != null ? necesidad.getCalendario().getNumeroCalendario() : null)
                .anioCalendario(necesidad != null && necesidad.getCalendario() != null ? necesidad.getCalendario().getAnioCalendario() : null)
                .fechaCreacion(asignacion.getFechaCreacion())
                .usuarioCreacion(asignacion.getUsuarioCreacion())
                .fechaActualizacion(asignacion.getFechaActualizacion())
                .usuarioActualizacion(asignacion.getUsuarioActualizacion())
                .build();
    }
}
