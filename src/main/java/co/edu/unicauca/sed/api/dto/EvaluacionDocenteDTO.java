package co.edu.unicauca.sed.api.dto;

import lombok.Data;
import java.util.List;
import co.edu.unicauca.sed.api.domain.Encuesta;
import co.edu.unicauca.sed.api.domain.EvaluacionEstudiante;

@Data
public class EvaluacionDocenteDTO {
    private Integer oidFuente;
    private String tipoCalificacion;
    private String observacion;
    private String firma;
    private Integer oidEstadoEtapaDesarrollo;
    private EvaluacionEstudiante evaluacionEstudiante;
    private Encuesta encuesta;
    private List<EncuestaPreguntaDTO> preguntas;
}
