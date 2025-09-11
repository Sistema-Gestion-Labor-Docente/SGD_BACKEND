package co.edu.unicauca.sgd.api.service.actividad.laborDocente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTORequest;
import co.edu.unicauca.sgd.api.dto.actividad.laborDocente.CargoActividadDTOResponse;

public interface CargoActividadService {

    ApiResponse<Page<CargoActividadDTOResponse>> obtenerTodos(String nombre, String tipo, Integer oidTipoActividad, Pageable pageable);
    ApiResponse<CargoActividadDTOResponse> buscarPorId(Integer oid);
    ApiResponse<CargoActividadDTOResponse> guardar(CargoActividadDTORequest request);
    ApiResponse<CargoActividadDTOResponse> actualizar(Integer oid, CargoActividadDTORequest request);
    ApiResponse<Void> eliminar(Integer oid);
    
}
