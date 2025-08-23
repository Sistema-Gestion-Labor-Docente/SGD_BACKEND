package co.edu.unicauca.sgd.api.dto.calendario;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;

@Data
@NoArgsConstructor
public class FechaDTORequest {

    @NotNull
    private Integer oidNombreFecha;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicial;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFin;

    @NotNull
    private TipoFechaEnum tipo;

    @NotNull
    private Integer oidCalendario;
}
