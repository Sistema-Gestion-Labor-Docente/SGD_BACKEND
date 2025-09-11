package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioActividadCalendarioDTORequest {

    private Integer oidActividad;
    private Integer oidTipoActividad;
    private Integer oidEstadoActividad;
    private String nombreActividad;
    private Float horas;
    private Float semanas;
    private List<AtributoExtendidoDTO> atributos;

    // Relación
    private Integer oidCalendario;
    private List<Integer> oidsUsuarios;
}
