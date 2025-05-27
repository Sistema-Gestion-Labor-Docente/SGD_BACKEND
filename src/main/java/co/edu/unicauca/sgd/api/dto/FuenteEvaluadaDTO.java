package co.edu.unicauca.sgd.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuenteEvaluadaDTO {
    private Integer oidFuente;
    private List<PreguntaCalificadaDTO> preguntas;
}
