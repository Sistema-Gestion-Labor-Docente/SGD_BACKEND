package co.edu.unicauca.sed.api.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO para manejar la información de una fuente de tipo Informe de Administración.
 */
@Data
public class InformeAdministracionFuenteDTO {

    private Integer oidFuente;
    private String tipoCalificacion;
    private Float calificacion;
    private String observacion;
    private List<InformeAdministracionDTO> informesAdministracion;
}
