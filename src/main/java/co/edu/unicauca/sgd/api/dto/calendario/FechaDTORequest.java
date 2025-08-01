package co.edu.unicauca.sgd.api.dto.calendario;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;

@Data
@NoArgsConstructor
public class FechaDTORequest {

    @NotBlank
    private String nombre;

    @NotNull
    private LocalDateTime fechaInicial;

    @NotNull
    private LocalDateTime fechaFin;

    @NotNull
    private TipoFechaEnum tipo;

    @NotNull
    private Integer oidCalendario;
}
