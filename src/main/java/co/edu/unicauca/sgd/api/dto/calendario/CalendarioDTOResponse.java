package co.edu.unicauca.sgd.api.dto.calendario;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalendarioDTOResponse {

    private Integer oidcalendario;
    private String nombreCalendario;
    private Float semanasClase;
    private Float semanasPreparacion;
    private Float horasTotales;
    private LocalDateTime fechaCreacion;
    private String usuarioCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioActualizacion;
    private String estado;
    private String observacion;

}
