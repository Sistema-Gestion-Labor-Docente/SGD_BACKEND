package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioActividadCalendarioUsuarioDTO {

    private Integer oidUsuario;
    private Integer oidCargoActividad;
    private Float horas;
}
