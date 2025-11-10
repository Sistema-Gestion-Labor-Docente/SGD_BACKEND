package co.edu.unicauca.sgd.api.service.actividad.laborDocente;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.DocenciaDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.UsuarioActividadCalendarioDTOResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.ValidacionHorasCargoDTOResponse;

public interface UsuarioActividadCalendarioService {

    ApiResponse<UsuarioActividadCalendarioDTOResponse> crearActividadConRelaciones(UsuarioActividadCalendarioDTORequest request);

    ApiResponse<UsuarioActividadCalendarioDTOResponse> actualizarActividadConRelaciones(Integer oidActividad, UsuarioActividadCalendarioDTORequest request);
    
    ApiResponse<Page<UsuarioActividadCalendarioDTOResponse>> listarActividadesConRelaciones(
            Integer oidCalendario,
            Integer oidDepartamento,
            Integer oidTipoActividad,
            Integer oidEstadoActividad,
            Integer oidUsuarioResponsable,
            Pageable pageable);
    
    ApiResponse<UsuarioActividadCalendarioDTOResponse> obtenerActividadConRelaciones(Integer oidActividad);
    
    ApiResponse<Void> eliminarActividad(Integer oidActividad);
    
    ApiResponse<Void> eliminarRelacion(Integer oidActividad, Integer oidUsuario, Integer oidCalendario);

    // Listar por tipo de actividad
    ApiResponse<Page<DocenciaDTOResponse>> listarPorTipoDocencia(
            Integer oidCalendario,
            Integer oidDepartamento,
            Integer oidUsuario,
            String tipoContratacion,
            Integer semestre,
            Pageable pageable);

    ApiResponse<ValidacionHorasCargoDTOResponse> validarCupoUsuariosEnCargo(
            Integer oidTipoActividad,
            Integer oidCargoActividad,
            Integer oidCalendario,
            List<Integer> oidsUsuarios);
    
}
