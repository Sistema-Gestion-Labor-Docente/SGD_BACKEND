package co.edu.unicauca.sed.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodoEvaluacionDTO {
    private Integer oidPeriodo;
    private String nombre;
    private List<ActividadDepartamentoDTO> departamentos;
}
