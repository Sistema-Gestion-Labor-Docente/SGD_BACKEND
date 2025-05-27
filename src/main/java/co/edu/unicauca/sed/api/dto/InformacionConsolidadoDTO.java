package co.edu.unicauca.sed.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class InformacionConsolidadoDTO {
    private Integer oidUsuario;
    private String nombreDocente;
    private String numeroIdentificacion;
    private String facultad;
    private String departamento;
    private Double calificacion;
    private String categoria;
    private String tipoContratacion;
    private String dedicacion;
    private String nombreArchivo;
    private String rutaArchivo;
    private Integer idPeriodoAcademico;
    private String periodoAcademico;
}
