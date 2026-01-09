package co.edu.unicauca.sgd.api.dto.reportes;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RldPdfRequest {

    private Integer oidDocente;
    private Integer oidCalendario;
}
