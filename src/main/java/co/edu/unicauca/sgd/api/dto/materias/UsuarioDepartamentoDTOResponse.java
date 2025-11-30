package co.edu.unicauca.sgd.api.dto.materias;

import java.time.LocalDateTime;

import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.HorasLaborDocenteDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioDepartamentoDTOResponse {

    private UsuarioDTO usuario;
    private Integer oidDepartamento;
    private String nombreDepartamento;
    private LocalDateTime fechaCreacion;

    /**
     * Total de horas asociadas a actividades (mantiene compatibilidad hacia atrás).
     */
    private Float totalHorasActividades;

    /**
     * Resumen de horas de labor docente:
     * - Horas asignadas por tipo de actividad y total.
     * - Horas disponibles por tipo de actividad y total.
     */
    private HorasLaborDocenteDTO horasLaborDocente;

}
