package co.edu.unicauca.sgd.api.dto.actividad.laborDocente;

import java.util.List;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsuarioActividadCalendarioDTORequest {

    private Integer oidActividad;
    private Integer oidTipoActividad;
    private Integer oidEstadoActividad;
    private String nombreActividad;
    private Float semanas;
    private List<AtributoExtendidoDTO> atributos;

    private Integer oidCalendario;
    @Valid
    private List<UsuarioActividadCalendarioUsuarioDTO> usuarios;
}
