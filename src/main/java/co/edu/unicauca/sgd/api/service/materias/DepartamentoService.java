package co.edu.unicauca.sgd.api.service.materias;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.DepartamentoDTOResponse;

public interface DepartamentoService {

    ApiResponse<Page<DepartamentoDTOResponse>> obtenerTodos(String nombre, Pageable pageable);

    ApiResponse<DepartamentoDTOResponse> buscarPorId(Integer oid);

    ApiResponse<DepartamentoDTOResponse> guardar(DepartamentoDTORequest request);

    ApiResponse<DepartamentoDTOResponse> actualizar(Integer oid, DepartamentoDTORequest request);
    
    ApiResponse<Void> eliminar(Integer oid);

}
