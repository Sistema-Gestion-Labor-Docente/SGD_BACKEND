package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeleccionadoDTORequest {

    @NotNull
    @Positive
    private Integer oidCalendario;

    @NotNull
    @Positive
    private Integer oidUsuario;

    private ContratacionEnum tipo;

    private Integer oidDepartamento;

    private String dedicacion;
}
