package co.edu.unicauca.sgd.api.service;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import co.edu.unicauca.sgd.api.domain.LaborDocente;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.LaborDocenteRequestDTO;

public interface LaborDocenteService {
    ApiResponse<Page<LaborDocente>> listarTodos(Pageable pageable);

    ApiResponse<LaborDocente> buscarPorId(Integer id);

    ApiResponse<Void> guardar(LaborDocenteRequestDTO dto);

    ApiResponse<Void> eliminar(Integer id);

    ResponseEntity<Resource> descargarDocumento(Integer oidUsuario);
}