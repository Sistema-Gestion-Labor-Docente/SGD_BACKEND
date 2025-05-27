package co.edu.unicauca.sgd.api.dto;

import co.edu.unicauca.sgd.api.domain.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EvaluadorAsignacionDTO {
    private Usuario evaluador;
    private boolean asignacionDefault;
}
