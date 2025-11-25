package co.edu.unicauca.sgd.api.dto.materias;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlanDTORequest {

    private String numero;
    private String estado;
    private LocalDate fechaAprobacion;
    private String acuerdo;
    private Integer oidPrograma;
    /**
     * Identificador del plan base desde el cual se copiarán las materias.
     * Es opcional; si es null no se realiza clonación.
     */
    private Integer oidPlanBase;

}
