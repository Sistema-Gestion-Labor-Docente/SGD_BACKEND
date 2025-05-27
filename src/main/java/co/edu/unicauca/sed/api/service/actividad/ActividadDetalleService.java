package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.dto.actividad.ActividadBaseDTO;

/**
 * Interface para definir los métodos de detalle de actividad.
 */
public interface ActividadDetalleService {

    /**
     * Genera el nombre de la actividad basado en su tipo y atributos.
     *
     * @param actividadDTO Datos de la actividad.
     * @return Nombre generado de la actividad.
     */
    String generarNombreActividad(ActividadBaseDTO actividadDTO);
}
