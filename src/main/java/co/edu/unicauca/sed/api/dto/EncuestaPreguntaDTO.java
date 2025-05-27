package co.edu.unicauca.sed.api.dto;

import lombok.Data;

/**
 * DTO para manejar las preguntas y respuestas de una Encuesta.
 */
@Data
public class EncuestaPreguntaDTO {
    private Integer oidPregunta;
    private String respuesta;
    private Integer porcentaje;
}
