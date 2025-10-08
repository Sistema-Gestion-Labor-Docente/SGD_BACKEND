package co.edu.unicauca.sgd.api.dto.materias;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MateriaDTORequest {

    private String oidMateria;
    private String codigo;
    private String nombre;
    private Integer semestre;
    private Integer horasSemana;
    private Integer oidDepartamento;
    private Integer oidPlan;
    private Integer idCorrequisito;

}
