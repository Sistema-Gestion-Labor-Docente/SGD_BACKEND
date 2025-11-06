package co.edu.unicauca.sgd.api.dto.necesidades;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AsignacionDTOResponse {
    private Integer oidAsignacion;
    private Integer oidNecesidad;
    private Integer oidSeleccionado;
    private Integer oidActividad;

    private Float horasDocencia;
    private Float semanasDocencia;
    private Float horasPreparacion;
    private Float semanasPreparacion;

    private String nombreActividad;
    private String nombreDocente;
    private String codigoMateria;
    private String nombreMateria;
    private String grupo;
    private Integer numeroCalendario;
    private String anioCalendario;

    private LocalDateTime fechaCreacion;
    private String usuarioCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioActualizacion;
}
