package co.edu.unicauca.sgd.api.service.evaluacion_docente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.domain.EncuestaRespuesta;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.EncuestaPreguntaDTO;

public interface EncuestaRespuestaService {

    ApiResponse<Page<EncuestaRespuesta>> buscarTodos(Pageable pageable);

    ApiResponse<EncuestaRespuesta> buscarPorId(Integer oid);

    ApiResponse<EncuestaRespuesta> guardar(EncuestaPreguntaDTO encuestaPreguntaDTO, Integer oidEncuesta, Integer oidPregunta);

    ApiResponse<Void> eliminar(Integer oid);
}
