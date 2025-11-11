package co.edu.unicauca.sgd.api.service.necesidad;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.AsignacionDTOResponse;

public interface AsignacionService {

    ApiResponse<Page<AsignacionDTOResponse>> listar(Integer oidCalendario,
                                                    Integer oidDepartamento,
                                                    Integer oidNecesidad,
                                                    Integer oidSeleccionado,
                                                    Pageable pageable);

    ApiResponse<AsignacionDTOResponse> buscarPorId(Integer oidAsignacion);

    ApiResponse<AsignacionDTOResponse> crear(AsignacionDTORequest request);

    ApiResponse<AsignacionDTOResponse> actualizar(Integer oidAsignacion, AsignacionDTORequest request);

    ApiResponse<Void> eliminar(Integer oidAsignacion);
}
