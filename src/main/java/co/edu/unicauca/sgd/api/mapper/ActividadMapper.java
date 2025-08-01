package co.edu.unicauca.sgd.api.mapper;

import org.springframework.stereotype.Component;

import co.edu.unicauca.sgd.api.domain.*;
import co.edu.unicauca.sgd.api.dto.actividad.*;

/**
 * Clase para mapear entre entidades y DTOs relacionados con actividades.
 */
@Component
public class ActividadMapper {

    /**
     * Convierte un DTO de tipo ActividadBaseDTO a una entidad Actividad.
     *
     * @param actividadDTO El DTO a convertir.
     * @return La entidad Actividad correspondiente.
     */
    public Actividad convertToEntity(ActividadBaseDTO actividadDTO) {
        Actividad actividad = new Actividad();

        // Mapear los campos básicos
        actividad.setOidActividad(actividadDTO.getOidActividad());
        actividad.setNombreActividad(actividadDTO.getNombreActividad());
        actividad.setHoras(actividadDTO.getHoras());
        actividad.setSemanas(actividadDTO.getSemanas());
        actividad.setInformeEjecutivo(actividadDTO.getInformeEjecutivo());
        actividad.setFechaCreacion(actividadDTO.getFechaCreacion());
        actividad.setFechaActualizacion(actividadDTO.getFechaActualizacion());
        actividad.setIdLaborDocente(actividadDTO.getIdLaborDocente());

        // Mapear el tipo de actividad si está presente
        if (actividadDTO.getTipoActividad() != null) {
            TipoActividad tipoActividad = new TipoActividad();
            tipoActividad.setOidTipoActividad(actividadDTO.getTipoActividad().getOidTipoActividad());
            actividad.setTipoActividad(tipoActividad);
        }

        // Mapear el estado de la actividad si está presente
        if (actividadDTO.getOidEstadoActividad() != null) {
            EstadoActividad estadoActividad = new EstadoActividad();
            estadoActividad.setOidEstadoActividad(actividadDTO.getOidEstadoActividad());
            actividad.setEstadoActividad(estadoActividad);
        }

        return actividad;
    }

    /**
     * Actualiza los campos básicos de la actividad.
     *
     * @param actividadExistente La actividad existente.
     * @param actividadDTO       DTO con los datos actualizados.
     */
    public void actualizarCamposBasicos(Actividad actividadExistente, ActividadBaseDTO actividadDTO) {
        actividadExistente.setNombreActividad(actividadDTO.getNombreActividad());
        actividadExistente.setHoras(actividadDTO.getHoras());
        actividadExistente.setSemanas(actividadDTO.getSemanas());
        actividadExistente.setInformeEjecutivo(actividadDTO.getInformeEjecutivo());
    }
}
