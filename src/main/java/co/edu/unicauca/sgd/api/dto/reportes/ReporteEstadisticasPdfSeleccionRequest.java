package co.edu.unicauca.sgd.api.dto.reportes;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReporteEstadisticasPdfSeleccionRequest {

    private Integer oidCalendario;
    private Integer oidDepartamento;
    private Integer oidPrograma;
    private List<String> graficos;
}
