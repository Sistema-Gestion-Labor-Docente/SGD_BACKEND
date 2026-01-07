package co.edu.unicauca.sgd.api.dto.reportes;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReporteEstadisticasPdfRequest {

    private String titulo;
    private String subtitulo;
    private String meta;
    private String footer;
    private List<ReporteGraficoPdfRequest> graficos;
}
