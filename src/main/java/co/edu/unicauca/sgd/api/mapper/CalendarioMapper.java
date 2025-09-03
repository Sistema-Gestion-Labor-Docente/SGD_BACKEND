package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;

@Component
public class CalendarioMapper {

    public Calendario convertToEntity(CalendarioDTORequest dto) {
        Calendario calendario = new Calendario();
        calendario.setAnioCalendario(dto.getAnioCalendario());
        calendario.setNumeroCalendario(dto.getNumeroCalendario());
        calendario.setSemanasClase(dto.getSemanasClase());
        calendario.setSemanasPreparacion(dto.getSemanasPreparacion());
        calendario.setHorasPlanta(dto.getHorasPlanta());
        calendario.setHorasCatedra(dto.getHorasCatedra());
        calendario.setHorasOcasionales(dto.getHorasOcasionales());
        calendario.setHorasBecarioPracticante(dto.getHorasBecarioPracticante());
        calendario.setEstado(dto.getEstado());
        calendario.setObservacion(dto.getObservacion());
        return calendario;
    }

    public void actualizarCamposBasicos(Calendario existente, CalendarioDTORequest dto) {
        existente.setAnioCalendario(dto.getAnioCalendario());
        existente.setNumeroCalendario(dto.getNumeroCalendario());
        existente.setSemanasClase(dto.getSemanasClase());
        existente.setSemanasPreparacion(dto.getSemanasPreparacion());
        existente.setHorasPlanta(dto.getHorasPlanta());
        existente.setHorasCatedra(dto.getHorasCatedra());
        existente.setHorasBecarioPracticante(dto.getHorasBecarioPracticante());
        existente.setHorasOcasionales(dto.getHorasOcasionales());
        existente.setEstado(dto.getEstado());
        existente.setObservacion(dto.getObservacion());
    }

    public CalendarioDTOResponse toResponse(Calendario entidad) {
        CalendarioDTOResponse dto = new CalendarioDTOResponse();
        dto.setOidcalendario(entidad.getOidcalendario());
        dto.setAnioCalendario(entidad.getAnioCalendario());
        dto.setNumeroCalendario(entidad.getNumeroCalendario());
        dto.setSemanasClase(entidad.getSemanasClase());
        dto.setSemanasPreparacion(entidad.getSemanasPreparacion());
        dto.setHorasPlanta(entidad.getHorasPlanta());
        dto.setHorasCatedra(entidad.getHorasCatedra());
        dto.setHorasOcasionales(entidad.getHorasOcasionales());
        dto.setHorasBecarioPracticante(entidad.getHorasBecarioPracticante());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setUsuarioCreacion(entidad.getUsuarioCreacion());
        dto.setFechaActualizacion(entidad.getFechaActualizacion());
        dto.setUsuarioActualizacion(entidad.getUsuarioActualizacion());
        dto.setEstado(entidad.getEstado());
        dto.setObservacion(entidad.getObservacion());
        return dto;
    }
}
