package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.domain.Encuesta;
import co.edu.unicauca.sed.api.domain.EvaluacionEstudiante;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.EvaluacionDocenteDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EncuestaService {
    
    ApiResponse<Page<Encuesta>> buscarTodos(Pageable pageable);

    ApiResponse<Encuesta> buscarPorId(Integer oid);

    ApiResponse<Encuesta> guardar(Encuesta encuesta);

    Encuesta guardarEncuesta(EvaluacionDocenteDTO dto, EvaluacionEstudiante evaluacionEstudiante);

    ApiResponse<Void> eliminar(Integer oid);
}
