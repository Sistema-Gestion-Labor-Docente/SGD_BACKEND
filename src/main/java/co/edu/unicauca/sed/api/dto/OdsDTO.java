package co.edu.unicauca.sed.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OdsDTO {
    private Integer oidAutoevaluacionOds;
    private Integer oidOds;
    private String resultado;
    private String documento;
}
