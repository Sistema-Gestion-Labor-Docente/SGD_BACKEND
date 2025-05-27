package co.edu.unicauca.sgd.api.service.evaluacion_docente;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unicauca.sgd.api.domain.EvaluacionEstudiante;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.EvaluacionDocenteDTO;
import co.edu.unicauca.sgd.api.dto.PeriodoEvaluacionDTO;

public interface EvaluacionEstudianteService {
    
    ApiResponse<Page<EvaluacionEstudiante>> buscarTodos(Pageable pageable);

    ApiResponse<Void> guardarEvaluacionDocente(EvaluacionDocenteDTO dto, MultipartFile documentoFuente, MultipartFile firmaEstudiante);

    ApiResponse<Object> obtenerEvaluacionEstudiante(Integer oidFuente);

    ApiResponse<List<PeriodoEvaluacionDTO>> obtenerEvaluacionesEstructuradas();
}

