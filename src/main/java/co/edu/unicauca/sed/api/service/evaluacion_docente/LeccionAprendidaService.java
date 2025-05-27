package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.dto.LeccionDTO;
import co.edu.unicauca.sed.api.domain.Autoevaluacion;
import java.util.List;

public interface LeccionAprendidaService {
    void guardar(List<LeccionDTO> lecciones, Autoevaluacion autoevaluacion);
    List<LeccionDTO> obtenerDescripcionesLecciones(Autoevaluacion autoevaluacion);
}
