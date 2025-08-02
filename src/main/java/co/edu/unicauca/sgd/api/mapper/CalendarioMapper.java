package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;

@Component
public class CalendarioMapper {

    public Calendario convertToEntity(CalendarioDTORequest dto) {
        Calendario calendario = new Calendario();
        calendario.setNombreCalendario(dto.getNombreCalendario());
        calendario.setSemanasClase(dto.getSemanasClase());
        calendario.setSemanasPreparacion(dto.getSemanasPreparacion());
        calendario.setHorasTotales(dto.getHorasTotales());
        calendario.setUsuarioCreacion(dto.getUsuarioCreacion());
        calendario.setUsuarioActualizacion(dto.getUsuarioActualizacion());
        calendario.setEstado(dto.getEstado());
        calendario.setObservacion(dto.getObservacion());
        return calendario;
    }

    public void actualizarCamposBasicos(Calendario existente, CalendarioDTORequest dto) {
        existente.setNombreCalendario(dto.getNombreCalendario());
        existente.setSemanasClase(dto.getSemanasClase());
        existente.setSemanasPreparacion(dto.getSemanasPreparacion());
        existente.setHorasTotales(dto.getHorasTotales());
        existente.setUsuarioActualizacion(dto.getUsuarioActualizacion());
        existente.setEstado(dto.getEstado());
        existente.setObservacion(dto.getObservacion());
    }

    public CalendarioDTOResponse toResponse(Calendario entidad) {
        CalendarioDTOResponse dto = new CalendarioDTOResponse();
        dto.setOidcalendario(entidad.getOidcalendario());
        dto.setNombreCalendario(entidad.getNombreCalendario());
        dto.setSemanasClase(entidad.getSemanasClase());
        dto.setSemanasPreparacion(entidad.getSemanasPreparacion());
        dto.setHorasTotales(entidad.getHorasTotales());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setUsuarioCreacion(entidad.getUsuarioCreacion());
        dto.setFechaActualizacion(entidad.getFechaActualizacion());
        dto.setUsuarioActualizacion(entidad.getUsuarioActualizacion());
        dto.setEstado(entidad.getEstado());
        dto.setObservacion(entidad.getObservacion());
        return dto;
    }
}
