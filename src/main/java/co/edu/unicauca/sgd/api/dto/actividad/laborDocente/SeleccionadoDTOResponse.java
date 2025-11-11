package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import java.time.LocalDateTime;

import co.edu.unicauca.sgd.api.dto.UsuarioDTO;
import co.edu.unicauca.sgd.api.enums.ContratacionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeleccionadoDTOResponse {
    private Integer oidSeleccionado;
    private Integer oidCalendario;
    private UsuarioDTO usuario;
    private ContratacionEnum tipo;
    private String dedicacion;
    private LocalDateTime fechaCreacion;
    private String usuarioCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioActualizacion;
}
