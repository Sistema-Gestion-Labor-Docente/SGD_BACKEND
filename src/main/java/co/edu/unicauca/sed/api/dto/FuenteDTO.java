package co.edu.unicauca.sed.api.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FuenteDTO {

    private Integer oidFuente;
    private String tipoFuente;
    private Float calificacion;
    private String tipoCalificacion;
    private String nombreDocumentoFuente;
    private String nombreDocumentoInforme;
    private String observacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String estadoFuente;

    // Constructor
    public FuenteDTO(Integer oidFuente, String tipoFuente, Float calificacion, String tipoCalificacion, String nombreDocumentoFuente, String nombreDocumentoInforme, String observacion, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion, String estadoFuente) {
        this.oidFuente = oidFuente;
        this.tipoFuente = tipoFuente;
        this.calificacion = calificacion;
        this.tipoCalificacion = tipoCalificacion;
        this.nombreDocumentoFuente = nombreDocumentoFuente;
        this.nombreDocumentoInforme = nombreDocumentoInforme;
        this.observacion = observacion;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.estadoFuente = estadoFuente;
    }

    public FuenteDTO ( Integer oidFuente, String estadoFuente, Float calificacion, String tipoFuente) {
        this.oidFuente = oidFuente;
        this.estadoFuente = estadoFuente;
        this.calificacion = calificacion;
        this.tipoFuente = tipoFuente;
    }
}
