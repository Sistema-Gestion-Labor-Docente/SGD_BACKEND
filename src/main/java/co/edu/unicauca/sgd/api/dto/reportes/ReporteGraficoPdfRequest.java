package co.edu.unicauca.sgd.api.dto.reportes;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReporteGraficoPdfRequest {

    private String titulo;
    private String resumen;
    private String descripcion;
    private String aporte;
    private List<ReporteDatoGraficoRequest> datos;
}
