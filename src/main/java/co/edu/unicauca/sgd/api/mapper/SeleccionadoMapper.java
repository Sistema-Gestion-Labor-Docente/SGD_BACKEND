package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Seleccionado;
import co.edu.unicauca.sgd.api.domain.Usuario;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTOResponse;

@Component
public class SeleccionadoMapper {

    public SeleccionadoDTOResponse toResponse(Seleccionado s) {
        if (s == null) return null;

        return SeleccionadoDTOResponse.builder()
                .oidSeleccionado(s.getOidSeleccionado())
                .oidCalendario(s.getCalendario() != null ? s.getCalendario().getOidcalendario() : null)
                .oidUsuario(s.getUsuario() != null ? s.getUsuario().getOidUsuario() : null)
                .tipo(s.getTipo())
                .dedicacion(s.getDedicacion())
                .fechaCreacion(s.getFechaCreacion())
                .usuarioCreacion(s.getUsuarioCreacion())
                .fechaActualizacion(s.getFechaActualizacion())
                .usuarioActualizacion(s.getUsuarioActualizacion())
                .build();
    }

    public Seleccionado convertToEntity(SeleccionadoDTORequest r) {
        if (r == null) return null;

        Seleccionado s = new Seleccionado();

        // Relación Calendario: usamos un objeto con solo el OID para evitar fetch innecesario.
        if (r.getOidCalendario() != null) {
            Calendario calendario = new Calendario();
            calendario.setOidcalendario(r.getOidCalendario());
            s.setCalendario(calendario);
        }

        // Relación Usuario: tu entidad Usuario tiene un constructor que acepta oid (según lo compartido).
        if (r.getOidUsuario() != null) {
            // Si tu Usuario tiene constructor Usuario(Integer) lo usamos; si no, crea uno y setOidUsuario.
            Usuario usuario = new Usuario(r.getOidUsuario());
            s.setUsuario(usuario);
        }

        s.setTipo(r.getTipo());
        s.setDedicacion(r.getDedicacion());

        return s;
    }
}
