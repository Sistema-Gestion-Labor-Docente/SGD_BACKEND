package co.edu.unicauca.sed.api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoricoCalificacionesDTO {
    private Integer oidUsuario;
    private String nombreDocente;
    private String numeroIdentificacion;
    private String facultad;
    private String departamento;
    private String categoria;
    private String tipoContratacion;
    private String dedicacion;
    private Double promedioGeneral;
    private List<CalificacionPorPeriodoDTO> calificacionesPorPeriodo;
}
