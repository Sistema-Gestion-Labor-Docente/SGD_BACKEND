package co.edu.unicauca.sgd.api.service.materias;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.MateriaDTOResponse;

public interface MateriaService {

    ApiResponse<Page<MateriaDTOResponse>> obtenerTodos(
        String oidmateria, String codigo, String nombre,
        Integer semestre, Integer oidDepartamento, Integer oidPlan,
        Pageable pageable);

    ApiResponse<MateriaDTOResponse> buscarPorId(Integer id);

    ApiResponse<MateriaDTOResponse> guardar(MateriaDTORequest request);

    ApiResponse<MateriaDTOResponse> actualizar(Integer id, MateriaDTORequest request);

    ApiResponse<Void> eliminar(Integer id);

}
