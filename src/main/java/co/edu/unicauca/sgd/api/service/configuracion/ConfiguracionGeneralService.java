package co.edu.unicauca.sgd.api.service.configuracion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.configuracion.ConfiguracionGeneralDTORequest;
import co.edu.unicauca.sgd.api.dto.configuracion.ConfiguracionGeneralDTOResponse;

public interface ConfiguracionGeneralService {

    ApiResponse<Page<ConfiguracionGeneralDTOResponse>> obtenerTodos(Pageable pageable);

    ApiResponse<ConfiguracionGeneralDTOResponse> buscarPorId(Integer oid);

    ApiResponse<ConfiguracionGeneralDTOResponse> guardar(ConfiguracionGeneralDTORequest dto);

    ApiResponse<ConfiguracionGeneralDTOResponse> actualizar(Integer oid, ConfiguracionGeneralDTORequest dto);

    ApiResponse<Void> eliminar(Integer oid);

    ApiResponse<String> obtenerSedUrl();
}
