package co.edu.unicauca.sed.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgrupacionEvaluacionDTO {
    private Integer oidPeriodo;
    private String nombrePeriodo;
    private Integer oidTipoActividad;
    private String nombreTipoActividad;
    private String departamento;
    private List<FuenteEvaluadaDTO> fuentes;
}
