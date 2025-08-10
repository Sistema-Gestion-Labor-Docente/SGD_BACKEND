package co.edu.unicauca.sgd.api.service.materias;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.PlanDTOResponse;

public interface PlanService {
    ApiResponse<Page<PlanDTOResponse>> obtenerTodos(String numero, String estado, Integer oidPrograma, Pageable pageable);
    ApiResponse<PlanDTOResponse> buscarPorId(Integer oid);
    ApiResponse<PlanDTOResponse> guardar(PlanDTORequest request);
    ApiResponse<PlanDTOResponse> actualizar(Integer oid, PlanDTORequest request);
    ApiResponse<Void> eliminar(Integer oid);

}
