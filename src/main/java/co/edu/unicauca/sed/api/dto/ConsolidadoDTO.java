package co.edu.unicauca.sed.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.List;

/**
 * DTO para consolidar información consolidada de un docente.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidadoDTO {

    private String nombreDocente;
    private String correoElectronico;
    private String numeroIdentificacion;
    private String periodoAcademico;
    private String facultad;
    private String departamento;
    private String categoria;
    private String tipoContratacion;
    private String dedicacion;
    private Map<String, List<Map<String, Object>>> actividades;
    private Float horasTotales;
    private Float porcentajeEvaluacionCompletado;
    private Double totalPorcentaje;
    private Double totalAcumulado;
    private int currentPage;
    private int pageSize;
    private int totalItems;
    private int totalPages;
}
