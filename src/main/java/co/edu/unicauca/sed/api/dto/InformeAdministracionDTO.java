package co.edu.unicauca.sed.api.dto;

import lombok.Data;

/**
 * DTO para manejar cada respuesta (calificación) asociada a un objetivo componente en el informe de administración.
 */
@Data
public class InformeAdministracionDTO {

    private Integer oidObjetivoComponente;
    private Float calificacion;
}
