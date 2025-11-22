package co.edu.unicauca.sgd.api.dto.materias;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DepartamentoDTORequest {

    private String nombre;
    private String facultad;
    private Integer jefeOidUsuario;

}
