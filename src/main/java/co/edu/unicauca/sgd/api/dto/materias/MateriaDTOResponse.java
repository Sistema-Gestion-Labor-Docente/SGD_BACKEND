package co.edu.unicauca.sgd.api.dto.materias;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MateriaDTOResponse {

    private Integer idMateria;
    private String oidMateria;
    private String codigo;
    private String nombre;
    private Integer semestre;
    private Integer horasSemana;

    private Integer oidDepartamento;
    private String nombreDepartamento;

    private Integer oidPlan;
    private String numeroPlan;

    private Integer idCorrequisito;
    private String oidCorrequisito;
    private String nombreCorrequisito;

    private LocalDateTime fechaCreacion;
    private String usuarioCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioActualizacion;

}
