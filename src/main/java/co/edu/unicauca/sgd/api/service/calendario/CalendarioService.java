package co.edu.unicauca.sgd.api.service.calendario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.domain.Calendario;
import co.edu.unicauca.sgd.api.dto.ApiResponse;

public interface CalendarioService {

    ApiResponse<Page<Calendario>> obtenerTodos(String nombreCalendario, String estado, Pageable pageable);

    ApiResponse<Calendario> buscarPorId(Integer oid);

    ApiResponse<Calendario> guardar(Calendario calendario);

    ApiResponse<Calendario> actualizar(Integer id, Calendario calendarioActualizado);

    ApiResponse<Void> eliminar(Integer oid);
}

