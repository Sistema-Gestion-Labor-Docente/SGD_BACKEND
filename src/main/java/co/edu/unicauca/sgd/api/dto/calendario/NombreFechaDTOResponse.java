package co.edu.unicauca.sgd.api.dto.calendario;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NombreFechaDTOResponse {

    private Integer oidNombreFecha;
    private String nombre;

    // Auditoría
    private LocalDateTime fechaCreacion;
    private String usuarioCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioActualizacion;

}
