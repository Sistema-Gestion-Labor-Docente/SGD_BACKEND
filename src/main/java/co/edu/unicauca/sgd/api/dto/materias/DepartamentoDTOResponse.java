package co.edu.unicauca.sgd.api.dto.materias;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DepartamentoDTOResponse {

    private Integer oidDepartamento;
    private String nombre;
    private String facultad;

    private Integer jefeOidUsuario;
    private String jefeNombre;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioCreacion;
    private String usuarioActualizacion;

}
