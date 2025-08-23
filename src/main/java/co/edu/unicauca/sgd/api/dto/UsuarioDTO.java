package co.edu.unicauca.sgd.api.dto;

import java.util.List;

import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTOResponse;
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
    private DepartamentoDTOResponse departamento;
    private List<RolDTO> roles;

    private ProgramaDTOResponse programaCoordinador;
    private DepartamentoDTOResponse departamentoJefatura; 

    // Construnctor usado en ActividadDTOServiceImpl
    public UsuarioDTO(Integer oidUsuario, String identificacion, String nombres, String apellidos, List<RolDTO> roles) {
        this.oidUsuario = oidUsuario;
        this.identificacion = identificacion;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.roles = roles;
    }
}
