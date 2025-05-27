package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.domain.EvaluacionEstudiante;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.EvaluacionDocenteDTO;
import co.edu.unicauca.sed.api.dto.PeriodoEvaluacionDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface EvaluacionEstudianteService {
    
    ApiResponse<Page<EvaluacionEstudiante>> buscarTodos(Pageable pageable);

    ApiResponse<Void> guardarEvaluacionDocente(EvaluacionDocenteDTO dto, MultipartFile documentoFuente, MultipartFile firmaEstudiante);

    ApiResponse<Object> obtenerEvaluacionEstudiante(Integer oidFuente);

    ApiResponse<List<PeriodoEvaluacionDTO>> obtenerEvaluacionesEstructuradas();
}

