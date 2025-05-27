package co.edu.unicauca.sed.api.dto;

import lombok.Data;
@Data
public class FuenteCreateDTO {
    private String tipoFuente;
    private Float calificacion;
    private String tipoCalificacion;
    private Integer oidActividad;
    private String informeEjecutivo;
}
