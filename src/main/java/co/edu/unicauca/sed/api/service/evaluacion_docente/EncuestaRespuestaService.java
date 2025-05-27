package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.domain.EncuestaRespuesta;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.EncuestaPreguntaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EncuestaRespuestaService {

    ApiResponse<Page<EncuestaRespuesta>> buscarTodos(Pageable pageable);

    ApiResponse<EncuestaRespuesta> buscarPorId(Integer oid);

    ApiResponse<EncuestaRespuesta> guardar(EncuestaPreguntaDTO encuestaPreguntaDTO, Integer oidEncuesta, Integer oidPregunta);

    ApiResponse<Void> eliminar(Integer oid);
}
