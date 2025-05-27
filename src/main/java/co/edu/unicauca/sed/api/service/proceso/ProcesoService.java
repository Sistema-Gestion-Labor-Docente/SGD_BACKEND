package co.edu.unicauca.sed.api.service.proceso;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.Proceso;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ProcesoService {

    String TIPO_CONSOLIDADO = "CONSOLIDADO";

    ApiResponse<Page<Proceso>> buscarTodos(Integer evaluadorId, Integer evaluadoId, Integer idPeriodo,
                                           String nombreProceso, LocalDateTime fechaCreacion,
                                           LocalDateTime fechaActualizacion, Pageable pageable);

    ApiResponse<Proceso> buscarPorId(Integer oid);

    ApiResponse<Proceso> guardar(Proceso proceso);

    ApiResponse<Proceso> actualizar(Integer oid, Proceso proceso);

    ApiResponse<Void> eliminar(Integer oid);

    ApiResponse<Void> guardarProceso(Actividad actividad);

    Proceso buscarProcesoExistente(Integer idEvaluador, Integer idEvaluado, Integer idPeriodoAcademico, String nombreProceso);

    Proceso crearNuevoProceso(Integer idEvaluador, Integer idEvaluado, Integer idPeriodoAcademico);
}
