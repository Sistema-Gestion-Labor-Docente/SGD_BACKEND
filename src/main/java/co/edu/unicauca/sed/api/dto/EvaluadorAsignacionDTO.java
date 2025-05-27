package co.edu.unicauca.sed.api.dto;

import co.edu.unicauca.sed.api.domain.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EvaluadorAsignacionDTO {
    private Usuario evaluador;
    private boolean asignacionDefault;
}
