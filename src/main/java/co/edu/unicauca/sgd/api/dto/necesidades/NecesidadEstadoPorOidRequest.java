package co.edu.unicauca.sgd.api.dto.necesidades;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;

@Data
@NoArgsConstructor
public class NecesidadEstadoPorOidRequest {

    @NotEmpty(message = "Debe proporcionar al menos una necesidad")
    private List<Integer> oidNecesidades;

    @NotNull(message = "El estado origen es obligatorio")
    private EstadoNecesidad estadoOrigen;

    @NotNull(message = "El estado destino es obligatorio")
    private EstadoNecesidad estadoDestino;
}
