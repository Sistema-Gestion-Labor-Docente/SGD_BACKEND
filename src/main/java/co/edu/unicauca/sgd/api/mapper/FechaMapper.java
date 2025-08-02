package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;

@Component
public class FechaMapper {

    public Fecha convertToEntity(FechaDTORequest dto, Calendario calendario) {
        Fecha fecha = new Fecha();
        fecha.setNombre(dto.getNombre());
        fecha.setFechaInicial(dto.getFechaInicial());
        fecha.setFechaFin(dto.getFechaFin());
        fecha.setTipo(dto.getTipo());
        fecha.setCalendario(calendario);
        return fecha;
    }

    public void actualizarCamposBasicos(Fecha existente, FechaDTORequest dto, Calendario calendario) {
        existente.setNombre(dto.getNombre());
        existente.setFechaInicial(dto.getFechaInicial());
        existente.setFechaFin(dto.getFechaFin());
        existente.setTipo(dto.getTipo());
        existente.setCalendario(calendario);
    }

    public FechaDTOResponse toResponse(Fecha entidad) {
        FechaDTOResponse dto = new FechaDTOResponse();
        dto.setOidFecha(entidad.getOidFecha());
        dto.setNombre(entidad.getNombre());
        dto.setFechaInicial(entidad.getFechaInicial());
        dto.setFechaFin(entidad.getFechaFin());
        dto.setTipo(entidad.getTipo());
        dto.setOidCalendario(entidad.getCalendario().getOidcalendario());
        dto.setNombreCalendario(entidad.getCalendario().getNombreCalendario());
        return dto;
    }
}

