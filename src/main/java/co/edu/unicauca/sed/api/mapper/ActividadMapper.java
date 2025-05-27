package co.edu.unicauca.sed.api.mapper;

import co.edu.unicauca.sed.api.domain.*;
import co.edu.unicauca.sed.api.dto.actividad.*;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        // Mapear el proceso si está presente
        if (actividadDTO.getOidProceso() != null) {
            Proceso proceso = new Proceso();
            proceso.setOidProceso(actividadDTO.getOidProceso());
            actividad.setProceso(proceso);
        }

        // Mapear el estado de la actividad si está presente
        if (actividadDTO.getOidEstadoActividad() != null) {
            EstadoActividad estadoActividad = new EstadoActividad();
            estadoActividad.setOidEstadoActividad(actividadDTO.getOidEstadoActividad());
            actividad.setEstadoActividad(estadoActividad);
        }

        // Mapear las fuentes si están presentes
        if (actividadDTO.getFuentes() != null) {
            List<Fuente> fuentes = actividadDTO.getFuentes().stream()
                    .map(fuenteDTO -> {
                        Fuente fuente = new Fuente();
                        fuente.setOidFuente(fuenteDTO.getOidFuente());
                        return fuente;
                    })
                    .sorted(Comparator.comparing(Fuente::getTipoFuente))
                    .collect(Collectors.toList());
            actividad.setFuentes(fuentes);
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
