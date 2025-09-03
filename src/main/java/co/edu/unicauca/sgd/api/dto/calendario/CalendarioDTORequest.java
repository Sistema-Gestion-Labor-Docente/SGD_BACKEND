package co.edu.unicauca.sgd.api.dto.calendario;

import jakarta.annotation.Nullable;
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

    @Nullable
    @Min(1)
    private Float semanasClase;

    @Nullable
    @Min(1)
    private Float semanasPreparacion;

    private Float horasPlanta;

    private Float horasCatedra;

    private Float horasOcasionales;

    private Float horasBecarioPracticante;

    private String estado;

    private String observacion;

}
