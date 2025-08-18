package co.edu.unicauca.sgd.api.dto.calendario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NombreFechaDTORequest {

    @NotBlank
    @Size(max = 255)
    private String nombre;

}
