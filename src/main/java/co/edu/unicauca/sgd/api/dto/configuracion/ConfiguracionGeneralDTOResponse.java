package co.edu.unicauca.sgd.api.dto.configuracion;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfiguracionGeneralDTOResponse {

    private Integer oidConfigGeneral;
    private String clave;
    private String valor;
    private boolean habilitado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
