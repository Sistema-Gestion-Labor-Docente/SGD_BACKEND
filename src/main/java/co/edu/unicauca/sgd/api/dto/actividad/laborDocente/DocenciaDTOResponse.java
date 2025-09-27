package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import java.util.List;

import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DocenciaDTOResponse {

    private ActividadBaseDTO actividad;
    private List<UsuarioDTO> usuarios;
    private Integer oidCalendario;
    private String nombreCalendario;

    // Campos específicos
    private Float cargaHorariaDocencia;
    private String asignatura;

}
