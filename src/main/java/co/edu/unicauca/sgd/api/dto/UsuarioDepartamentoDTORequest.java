package co.edu.unicauca.sgd.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioDepartamentoDTORequest {

    private Integer oidUsuario;
    private Integer oidDepartamento;

}
