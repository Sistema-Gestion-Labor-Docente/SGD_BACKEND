package co.edu.unicauca.sgd.api.dto.configuracion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfiguracionGeneralDTORequest {

    @NotBlank
    @Size(max = 150)
    private String clave;

    private String valor;

    @NotNull
    private Boolean habilitado;
}
