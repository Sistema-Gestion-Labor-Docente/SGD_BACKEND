package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.CargoActividad;
import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTOResponse;

@Component
public class CargoActividadMapper {

    public CargoActividad convertToEntity(CargoActividadDTORequest dto, TipoActividad tipoActividad) {
        CargoActividad entity = new CargoActividad();
        entity.setNombre(dto.getNombre());
        entity.setTipo(dto.getTipo());
        entity.setMaxHorasSemana(dto.getMaxHorasSemana());
        entity.setMaxActividades(dto.getMaxActividades());
        entity.setTipoActividad(tipoActividad);
        // usuarioCreacion y fechaCreacion se setean en service según el contexto
        return entity;
    }

    public void actualizarCamposBasicos(CargoActividad entity, CargoActividadDTORequest dto, TipoActividad tipoActividad) {
        entity.setNombre(dto.getNombre());
        entity.setTipo(dto.getTipo());
        entity.setMaxHorasSemana(dto.getMaxHorasSemana());
        entity.setMaxActividades(dto.getMaxActividades());
        entity.setTipoActividad(tipoActividad);
    }

    public CargoActividadDTOResponse toResponse(CargoActividad entity) {
        CargoActividadDTOResponse dto = new CargoActividadDTOResponse();
        dto.setOidCargoActividad(entity.getOidCargoActividad());
        dto.setNombre(entity.getNombre());
        dto.setTipo(entity.getTipo());
        dto.setMaxHorasSemana(entity.getMaxHorasSemana());
        dto.setMaxActividades(entity.getMaxActividades());
        dto.setOidTipoActividad(entity.getTipoActividad().getOidTipoActividad());
        dto.setNombreTipoActividad(entity.getTipoActividad().getNombre());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setUsuarioCreacion(entity.getUsuarioCreacion());
        dto.setFechaActualizacion(entity.getFechaActualizacion());
        dto.setUsuarioActualizacion(entity.getUsuarioActualizacion());
        return dto;
    }
}

