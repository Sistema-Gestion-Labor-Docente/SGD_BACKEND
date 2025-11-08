package co.edu.unicauca.sgd.api.service.materias;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.UsuarioDepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.UsuarioDepartamentoDTOResponse;

public interface UsuarioDepartamentoService {

    ApiResponse<Page<UsuarioDepartamentoDTOResponse>> obtenerTodos(Integer oidUsuario, Integer oidDepartamento, Pageable pageable);

    ApiResponse<UsuarioDepartamentoDTOResponse> buscarPorUsuario(Integer oidUsuario);

    ApiResponse<UsuarioDepartamentoDTOResponse> guardar(UsuarioDepartamentoDTORequest request);

    ApiResponse<UsuarioDepartamentoDTOResponse> actualizar(Integer oidUsuario, UsuarioDepartamentoDTORequest request);

    ApiResponse<Void> eliminar(Integer oidUsuario);

    ApiResponse<List<UsuarioDepartamentoDTOResponse>> obtenerProfesoresPorTipoActividad(String filtro, Integer oidDepartamento);
    
}
