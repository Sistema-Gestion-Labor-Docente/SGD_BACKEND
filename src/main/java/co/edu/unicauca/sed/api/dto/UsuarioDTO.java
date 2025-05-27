package co.edu.unicauca.sed.api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {
    private Integer oidUsuario;
    private String identificacion;
    private String nombres;
    private String apellidos;
    private List<RolDTO> roles;
}
