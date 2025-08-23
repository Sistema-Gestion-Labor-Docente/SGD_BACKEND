package co.edu.unicauca.sgd.api.service.calendario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.FechaDTOResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;

public interface FechaService {

    ApiResponse<Page<FechaDTOResponse>> obtenerTodas(TipoFechaEnum tipo, Pageable pageable);

    ApiResponse<FechaDTOResponse> buscarPorId(Integer oid);

    ApiResponse<FechaDTOResponse> guardar(FechaDTORequest dto);

    ApiResponse<FechaDTOResponse> actualizar(Integer id, FechaDTORequest dto);

    ApiResponse<Void> eliminar(Integer oid);
    
}
