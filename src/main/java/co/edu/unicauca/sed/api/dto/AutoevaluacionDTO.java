package co.edu.unicauca.sed.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class AutoevaluacionDTO {
    private Integer oidFuente;
    private String tipoCalificacion;
    private Float calificacion;
    private String descripcion;
    private String observacion;
    private List<OdsDTO> odsSeleccionados;
    private List<LeccionDTO> leccionesAprendidas;
    private List<OportunidadMejoraDTO> oportunidadesMejora;
}
