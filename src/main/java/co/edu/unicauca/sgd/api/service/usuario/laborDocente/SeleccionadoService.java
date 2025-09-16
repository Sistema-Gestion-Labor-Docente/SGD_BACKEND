package co.edu.unicauca.sgd.api.service.usuario.laborDocente;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.SeleccionadoDTOResponse;

public interface SeleccionadoService {

    ApiResponse<Page<SeleccionadoDTOResponse>> obtenerTodos(Integer oidCalendario,
                                                          Integer oidDepartamento,
                                                          Pageable pageable);
    
    ApiResponse<SeleccionadoDTOResponse> buscarPorId(Integer oid);

    ApiResponse<SeleccionadoDTOResponse> guardar(SeleccionadoDTORequest request);

    ApiResponse<SeleccionadoDTOResponse> actualizar(Integer oid, SeleccionadoDTORequest request);

    ApiResponse<Void> eliminar(Integer oid);

}
