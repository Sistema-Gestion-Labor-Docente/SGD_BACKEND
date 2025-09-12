package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CargoActividadDTORequest {

    @NotNull
    private String nombre;

    @NotNull
    private String tipo;

    @NotNull
    private Float maxHorasSemana;

    @NotNull
    private Integer oidTipoActividad;
}
