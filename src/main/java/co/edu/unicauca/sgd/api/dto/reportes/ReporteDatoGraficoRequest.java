package co.edu.unicauca.sgd.api.dto.reportes;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReporteDatoGraficoRequest {

    private String etiqueta;
    private Double valor;
}
