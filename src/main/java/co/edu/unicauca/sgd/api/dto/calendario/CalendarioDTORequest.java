package co.edu.unicauca.sgd.api.dto.calendario;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalendarioDTORequest {

    @NotBlank
    private String nombreCalendario;

    @NotNull
    private Float semanasClase;

    @NotNull
    private Float semanasPreparacion;

    @NotNull
    private Float horasTotales;

    @NotBlank
    private String usuarioCreacion;

    @NotBlank
    private String usuarioActualizacion;

    @NotBlank
    private String estado;

    private String observacion;

}
