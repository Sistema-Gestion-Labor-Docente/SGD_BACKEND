package co.edu.unicauca.sgd.api.dto.necesidades;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AsignacionDTORequest {

    @NotNull
    @Positive
    private Integer oidNecesidad;

    @NotNull
    @Positive
    private Integer oidSeleccionado;

    @NotNull
    @Positive
    private Integer oidEstadoActividad;

    private String nombreActividad;
}
