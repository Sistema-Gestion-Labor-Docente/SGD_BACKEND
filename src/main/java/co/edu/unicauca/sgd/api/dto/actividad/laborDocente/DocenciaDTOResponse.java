package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import java.util.List;

import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTOResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTOResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTOResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DocenciaDTOResponse {

    private ActividadBaseDTO actividad;
    private List<UsuarioDTO> usuarios;
    private Integer oidCalendario;
    private String nombreCalendario;
    private NecesidadDTOResponse necesidad;
    private MateriaDTOResponse materia;
    private AsignacionDTOResponse asignacion;

    // Campos específicos
    private Float cargaHorariaDocencia;
    private String asignatura;

}
