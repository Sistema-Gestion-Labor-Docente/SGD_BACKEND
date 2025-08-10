package co.edu.unicauca.sgd.api.service.materias;

import java.util.List;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.CorrequisitoListaResponse;
import co.edu.unicauca.sgd.api.dto.materias.CorrequisitoPairDTO;

public interface CorrequisitoService {

    ApiResponse<CorrequisitoListaResponse> listar(Integer idMateria);

    ApiResponse<Void> agregar(CorrequisitoPairDTO pair);
    
    ApiResponse<Void> eliminar(CorrequisitoPairDTO pair);

    ApiResponse<CorrequisitoListaResponse> reemplazar(Integer idMateria, List<Integer> nuevosCorrequisitos);

}
