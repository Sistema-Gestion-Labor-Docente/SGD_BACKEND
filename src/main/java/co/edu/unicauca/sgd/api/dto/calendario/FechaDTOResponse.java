package co.edu.unicauca.sgd.api.dto.calendario;

import java.time.LocalDateTime;

import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FechaDTOResponse {

    private Integer oidFecha;
    private String nombre;
    private LocalDateTime fechaInicial;
    private LocalDateTime fechaFin;
    private TipoFechaEnum tipo;
    private Integer oidCalendario;
    private String nombreCalendario;

}

