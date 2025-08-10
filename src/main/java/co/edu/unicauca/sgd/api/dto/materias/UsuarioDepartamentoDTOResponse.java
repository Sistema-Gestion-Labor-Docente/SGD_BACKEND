package co.edu.unicauca.sgd.api.dto.materias;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioDepartamentoDTOResponse {

    private Integer oidUsuario;
    private Integer oidDepartamento;
    private String nombreDepartamento;
    private LocalDateTime fechaCreacion;

}
