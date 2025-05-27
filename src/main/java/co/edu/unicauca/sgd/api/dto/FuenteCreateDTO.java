package co.edu.unicauca.sgd.api.dto;

import lombok.Data;
@Data
public class FuenteCreateDTO {
    private String tipoFuente;
    private Float calificacion;
    private String tipoCalificacion;
    private Integer oidActividad;
    private String informeEjecutivo;
}
