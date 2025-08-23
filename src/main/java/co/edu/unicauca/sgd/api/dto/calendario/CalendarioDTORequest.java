package co.edu.unicauca.sgd.api.dto.calendario;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalendarioDTORequest {

    @NotNull
    private String anioCalendario;

    @NotNull
    @Min(1)
    private Integer numeroCalendario;

    @NotBlank
    @Min(1)
    private Float semanasClase;

    @NotBlank
    @Min(1)
    private Float semanasPreparacion;

    @NotBlank
    private Float horasTotales;

    private String estado;

    private String observacion;

}
