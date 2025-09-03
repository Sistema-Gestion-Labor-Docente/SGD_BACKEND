package co.edu.unicauca.sgd.api.dto.calendario;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalendarioDTOResponse {

    private Integer oidcalendario;
    private String anioCalendario;
    private Integer numeroCalendario;
    private Float semanasClase;
    private Float semanasPreparacion;
    private Float horasPlanta;
    private Float horasCatedra;
    private Float horasOcasionales;
    private Float horasBecarioPracticante;

    private LocalDateTime fechaCreacion;
    private String usuarioCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioActualizacion;

    private String estado;
    private String observacion;

    private List<FechaDTOResponse> fechas;

}
