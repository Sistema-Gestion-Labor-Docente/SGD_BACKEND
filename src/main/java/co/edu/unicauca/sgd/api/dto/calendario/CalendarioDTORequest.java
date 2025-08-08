package co.edu.unicauca.sgd.api.dto.calendario;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalendarioDTORequest {

    @NotBlank
    private String anioCalendario;

    @NotNull
    private Integer numeroCalendario;

    @NotNull
    private Float semanasClase;

    @NotNull
    private Float semanasPreparacion;

    @NotNull
    private Float horasTotales;

    private String usuario;

    private String estado;

    private String observacion;

}
