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
    private Integer numeroCalendario;

    @NotBlank
    private Float semanasClase;

    @NotBlank
    private Float semanasPreparacion;

    @NotBlank
    private Float horasTotales;

    private String estado;

    private String observacion;

}
