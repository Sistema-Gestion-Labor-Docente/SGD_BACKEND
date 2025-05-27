package co.edu.unicauca.sed.api.dto.actividad;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InformacionActividadDTO {
    private Integer idActividad;
    private String nombreActividad;
    private String tipoActividad;
    private Float horasTotales;
    private String periodoAcademico;
}
