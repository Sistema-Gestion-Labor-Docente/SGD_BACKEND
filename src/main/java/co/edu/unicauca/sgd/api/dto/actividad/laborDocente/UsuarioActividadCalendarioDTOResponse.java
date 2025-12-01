package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import java.util.List;

import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.actividad.ActividadBaseDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.HorasLaborDocenteDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioActividadCalendarioDTOResponse {
    private ActividadBaseDTO actividad;

    private Integer oidCalendario;
    private String nombreCalendario;

    private List<UsuarioDTO> usuarios;

    /**
     * Detalle de la relación usuario-actividad-calendario:
     * incluye el cargo y las horas asignadas por usuario.
     */
    private List<UsuarioActividadCalendarioUsuarioDTO> usuariosActividad;

    /**
     * Resumen de horas de labor docente asociado a la actividad.
     */
    private HorasLaborDocenteDTO horasLaborDocente;
}
