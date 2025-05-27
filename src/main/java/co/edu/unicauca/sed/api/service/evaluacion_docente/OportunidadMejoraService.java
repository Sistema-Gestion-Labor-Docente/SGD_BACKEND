package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.dto.OportunidadMejoraDTO;
import co.edu.unicauca.sed.api.domain.Autoevaluacion;

import java.util.List;

public interface OportunidadMejoraService {
    void guardar(List<OportunidadMejoraDTO> mejoras, Autoevaluacion autoevaluacion);
}