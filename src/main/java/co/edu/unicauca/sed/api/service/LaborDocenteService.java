package co.edu.unicauca.sed.api.service;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.LaborDocenteRequestDTO;
import co.edu.unicauca.sed.api.domain.LaborDocente;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface LaborDocenteService {
    ApiResponse<Page<LaborDocente>> listarTodos(Pageable pageable);

    ApiResponse<LaborDocente> buscarPorId(Integer id);

    ApiResponse<Void> guardar(LaborDocenteRequestDTO dto);

    ApiResponse<Void> eliminar(Integer id);

    ResponseEntity<Resource> descargarDocumento(Integer oidUsuario);
}