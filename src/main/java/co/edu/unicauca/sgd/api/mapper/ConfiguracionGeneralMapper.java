package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.ConfiguracionGeneral;
import co.edu.unicauca.sgd.api.dto.configuracion.ConfiguracionGeneralDTORequest;
import co.edu.unicauca.sgd.api.dto.configuracion.ConfiguracionGeneralDTOResponse;

@Component
public class ConfiguracionGeneralMapper {

    public ConfiguracionGeneral toEntity(ConfiguracionGeneralDTORequest dto) {
        ConfiguracionGeneral entity = new ConfiguracionGeneral();
        entity.setClave(dto.getClave());
        entity.setValor(dto.getValor());
        entity.setHabilitado(Boolean.TRUE.equals(dto.getHabilitado()));
        return entity;
    }

    public void update(ConfiguracionGeneral entity, ConfiguracionGeneralDTORequest dto) {
        entity.setClave(dto.getClave());
        entity.setValor(dto.getValor());
        entity.setHabilitado(Boolean.TRUE.equals(dto.getHabilitado()));
    }

    public ConfiguracionGeneralDTOResponse toResponse(ConfiguracionGeneral entity) {
        ConfiguracionGeneralDTOResponse dto = new ConfiguracionGeneralDTOResponse();
        dto.setOidConfigGeneral(entity.getOidConfigGeneral());
        dto.setClave(entity.getClave());
        dto.setValor(entity.getValor());
        dto.setHabilitado(entity.isHabilitado());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setFechaActualizacion(entity.getFechaActualizacion());
        return dto;
    }
}
