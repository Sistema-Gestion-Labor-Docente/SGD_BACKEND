package co.edu.unicauca.sgd.api.service.evaluacion_docente;

import java.util.List;

import co.edu.unicauca.sgd.api.domain.Autoevaluacion;
import co.edu.unicauca.sgd.api.dto.LeccionDTO;

public interface LeccionAprendidaService {
    void guardar(List<LeccionDTO> lecciones, Autoevaluacion autoevaluacion);
    List<LeccionDTO> obtenerDescripcionesLecciones(Autoevaluacion autoevaluacion);
}
