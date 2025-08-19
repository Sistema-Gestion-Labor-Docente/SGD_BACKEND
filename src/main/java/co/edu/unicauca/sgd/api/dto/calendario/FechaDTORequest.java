package co.edu.unicauca.sgd.api.dto.calendario;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;

@Data
@NoArgsConstructor
public class FechaDTORequest {

    @NotNull
    private Integer oidNombreFecha;

    @NotBlank
    private LocalDateTime fechaInicial;

    @NotBlank
    private LocalDateTime fechaFin;

    @NotNull
    private TipoFechaEnum tipo;

    @NotNull
    private Integer oidCalendario;
}
