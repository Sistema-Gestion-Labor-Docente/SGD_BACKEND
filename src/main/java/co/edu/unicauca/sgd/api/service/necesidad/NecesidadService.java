package co.edu.unicauca.sgd.api.service.necesidad;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTOResponse;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NecesidadService {

    ApiResponse<Page<NecesidadDTOResponse>> obtenerTodos(
            Integer oidCalendario,
            Integer idMateria,
            EstadoNecesidad estado,
            Pageable pageable);

    ApiResponse<NecesidadDTOResponse> buscarPorId(Integer oid);

    ApiResponse<NecesidadDTOResponse> guardar(NecesidadDTORequest request);

    ApiResponse<NecesidadDTOResponse> actualizar(Integer oid, NecesidadDTORequest request);

    ApiResponse<Void> eliminar(Integer oid);

    ApiResponse<Map<String, Object>> cambiarEstadoMasivo(Integer oidCalendario,
                                                         EstadoNecesidad estadoOrigen,
                                                         EstadoNecesidad estadoDestino,
                                                         Integer oidPrograma,
                                                         Integer oidDepartamento);
}
