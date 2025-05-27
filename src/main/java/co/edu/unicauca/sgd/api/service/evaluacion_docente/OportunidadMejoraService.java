package co.edu.unicauca.sgd.api.service.evaluacion_docente;

import java.util.List;

import co.edu.unicauca.sgd.api.domain.Autoevaluacion;
import co.edu.unicauca.sgd.api.dto.OportunidadMejoraDTO;

public interface OportunidadMejoraService {
    void guardar(List<OportunidadMejoraDTO> mejoras, Autoevaluacion autoevaluacion);
}