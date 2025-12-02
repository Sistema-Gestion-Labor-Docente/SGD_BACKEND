package co.edu.unicauca.sgd.api.dto.actividad;

import java.time.LocalDateTime;
import java.util.List;

import co.edu.unicauca.sgd.api.domain.TipoActividad;
import co.edu.unicauca.sgd.api.dto.AtributoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActividadBaseDTO {

    private Integer oidActividad;
    private TipoActividad tipoActividad;
    private Integer oidEstadoActividad;
    private String nombreActividad;
    private Float semanas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private List<AtributoDTO> atributos;

}
