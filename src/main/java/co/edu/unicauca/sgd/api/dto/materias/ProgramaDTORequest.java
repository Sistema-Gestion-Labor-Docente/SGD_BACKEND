package co.edu.unicauca.sgd.api.dto.materias;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProgramaDTORequest {
    
    @NotNull
    private String nombre;
    private Integer coordinadorOidUsuario;
}
