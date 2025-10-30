package co.edu.unicauca.sgd.api.dto.materias;

import java.time.LocalDateTime;

import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioDepartamentoDTOResponse {

    private UsuarioDTO usuario;
    private Integer oidDepartamento;
    private String nombreDepartamento;
    private LocalDateTime fechaCreacion;
    private Float totalHorasActividades;

}
