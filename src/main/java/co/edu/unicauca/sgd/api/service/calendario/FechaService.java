package co.edu.unicauca.sgd.api.service.calendario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.domain.Fecha;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.enums.TipoFechaEnum;

public interface FechaService {

    ApiResponse<Page<Fecha>> obtenerTodas(String nombre, TipoFechaEnum tipo, Pageable pageable);

    ApiResponse<Fecha> buscarPorId(Integer oid);

    ApiResponse<Fecha> guardar(Fecha fecha);

    ApiResponse<Fecha> actualizar(Integer id, Fecha fechaActualizada);

    ApiResponse<Void> eliminar(Integer oid);
}
