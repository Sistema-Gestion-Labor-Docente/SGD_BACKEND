package co.edu.unicauca.sgd.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalificacionPorPeriodoDTO {
    private Integer idPeriodoAcademico;
    private String idPeriodo;
    private Double calificacion;
}
