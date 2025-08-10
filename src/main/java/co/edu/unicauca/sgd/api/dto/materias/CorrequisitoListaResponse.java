package co.edu.unicauca.sgd.api.dto.materias;

import java.util.List;
import lombok.Data;

@Data
public class CorrequisitoListaResponse {
    private Integer idMateria;
    private List<Integer> correquisitos;
}
