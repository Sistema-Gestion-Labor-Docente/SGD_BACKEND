package co.edu.unicauca.sgd.api.service.calendario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.NombreFechaDTOResponse;

public interface NombreFechaService {

    ApiResponse<Page<NombreFechaDTOResponse>> obtenerTodas(String nombre, Pageable pageable);

    ApiResponse<NombreFechaDTOResponse> buscarPorId(Integer oid);

    ApiResponse<NombreFechaDTOResponse> guardar(NombreFechaDTORequest dto);

    ApiResponse<NombreFechaDTOResponse> actualizar(Integer oid, NombreFechaDTORequest dto);

    ApiResponse<Void> eliminar(Integer oid);
}
