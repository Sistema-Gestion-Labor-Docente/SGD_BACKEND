package co.edu.unicauca.sgd.api.dto.materias;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlanDTOResponse {

    private Integer oidPlan;
    private String numero;
    private String estado;
    private LocalDate fechaAprobacion;
    private String acuerdo;

    private Integer oidPrograma;
    private String nombrePrograma;

    /**
     * Cantidad de materias asociadas a este plan.
     */
    private long cantidadMaterias;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioCreacion;
    private String usuarioActualizacion;

}
