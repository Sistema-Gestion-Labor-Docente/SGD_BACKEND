package co.edu.unicauca.sed.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaCalificadaDTO {
    private Integer oidPregunta;
    private String texto;
    private Float calificacion;
}
