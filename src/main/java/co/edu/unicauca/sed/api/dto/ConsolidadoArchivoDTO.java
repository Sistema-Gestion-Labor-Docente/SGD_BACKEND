package co.edu.unicauca.sed.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsolidadoArchivoDTO {
    private String nombreArchivo;
    private Integer oidConsolidado;
}
