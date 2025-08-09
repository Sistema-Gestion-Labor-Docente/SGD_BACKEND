package co.edu.unicauca.sgd.api.dto.materias;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProgramaDTOResponse {
    private Integer oidPrograma;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioCreacion;
    private String usuarioActualizacion;
}
