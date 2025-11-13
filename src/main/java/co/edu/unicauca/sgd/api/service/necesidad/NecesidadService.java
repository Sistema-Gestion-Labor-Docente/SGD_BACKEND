package co.edu.unicauca.sgd.api.service.necesidad;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadBulkCreateRequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTORequest;
import co.edu.unicauca.sgd.api.dto.necesidades.NecesidadDTOResponse;
import co.edu.unicauca.sgd.api.enums.EstadoNecesidad;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NecesidadService {

    ApiResponse<Page<NecesidadDTOResponse>> obtenerTodos(
            Integer oidCalendario,
            Integer idMateria,
            EstadoNecesidad estado,
            Integer oidPrograma,
            Integer oidDepartamento,
            String nombreMateria,
            Integer semestreMateria,
            String codigoMateria,
            Pageable pageable);

    ApiResponse<NecesidadDTOResponse> buscarPorId(Integer oid);

    ApiResponse<NecesidadDTOResponse> guardar(NecesidadDTORequest request);
    ApiResponse<List<NecesidadDTOResponse>> guardarMasivo(NecesidadBulkCreateRequest request);

    ApiResponse<NecesidadDTOResponse> actualizar(Integer oid, NecesidadDTORequest request);

    ApiResponse<Void> eliminar(Integer oid);

}
