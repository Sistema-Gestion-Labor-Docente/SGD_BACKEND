package co.edu.unicauca.sed.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoActividadFuentesDTO {
    private Integer oidTipoActividad;
    private String nombre;
    private List<FuenteEvaluadaDTO> fuentes;
}
