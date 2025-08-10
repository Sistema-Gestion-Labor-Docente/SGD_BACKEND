package co.edu.unicauca.sgd.api.service.materias;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.ProgramaDTOResponse;

public interface ProgramaService {
    ApiResponse<Page<ProgramaDTOResponse>> obtenerTodos(String nombre, Pageable pageable);
    ApiResponse<ProgramaDTOResponse> buscarPorId(Integer oid);
    ApiResponse<ProgramaDTOResponse> guardar(ProgramaDTORequest request);
    ApiResponse<ProgramaDTOResponse> actualizar(Integer oid, ProgramaDTORequest request);
    ApiResponse<Void> eliminar(Integer oid);
}
