package co.edu.unicauca.sed.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluacionEstudianteDTO {
    private Integer oidPeriodoAcademico;
    private String idPeriodo;
    private Integer oidTipoActividad;
    private String nombreTipoActividad;
    private String departamento;
    private String pregunta;
    private Float calificacion;
}
