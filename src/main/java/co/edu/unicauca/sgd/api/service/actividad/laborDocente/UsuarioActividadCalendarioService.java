package co.edu.unicauca.sgd.api.service.actividad.laborDocente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;

public interface UsuarioActividadCalendarioService {

    ApiResponse<UsuarioActividadCalendarioDTOResponse> crearActividadConRelaciones(UsuarioActividadCalendarioDTORequest request);

    ApiResponse<UsuarioActividadCalendarioDTOResponse> actualizarActividadConRelaciones(Integer oidActividad, UsuarioActividadCalendarioDTORequest request);
    
    ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>> listarActividadesConRelaciones(Pageable pageable);
    
    ApiResponse<UsuarioActividadCalendarioDTOResponse> obtenerActividadConRelaciones(Integer oidActividad);
    
    ApiResponse<Void> eliminarActividad(Integer oidActividad);
    
    ApiResponse<Void> eliminarRelacion(Integer oidActividad, Integer oidUsuario, Integer oidCalendario);
    
}
