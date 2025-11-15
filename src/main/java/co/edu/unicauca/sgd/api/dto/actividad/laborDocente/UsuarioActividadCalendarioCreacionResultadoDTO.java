package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioActividadCalendarioCreacionResultadoDTO {

    private Integer indice;
    private boolean exito;
    private String mensaje;
    private UsuarioActividadCalendarioDTOResponse actividad;
}
