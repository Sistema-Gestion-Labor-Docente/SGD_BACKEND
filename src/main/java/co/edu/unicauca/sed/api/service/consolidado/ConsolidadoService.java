package co.edu.unicauca.sed.api.service.consolidado;

import co.edu.unicauca.sed.api.domain.Consolidado;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.ConsolidadoArchivoDTO;
import co.edu.unicauca.sed.api.dto.ConsolidadoDTO;
import co.edu.unicauca.sed.api.dto.HistoricoCalificacionesDTO;
import co.edu.unicauca.sed.api.dto.InformacionConsolidadoDTO;
import co.edu.unicauca.sed.api.dto.actividad.ActividadPaginadaDTO;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz para definir los métodos del servicio de Consolidado.
 */
public interface ConsolidadoService {

    ApiResponse<Page<InformacionConsolidadoDTO>> findAll(Pageable pageable, Boolean ascendingOrder, Integer idPeriodoAcademico, 
        Integer idUsuario, String nombre, String identificacion, String facultad, String departamento, String categoria);

    ApiResponse<Consolidado> findByOid(Integer oid);

    ApiResponse<Void> updateAllFromConsolidado(Integer oidConsolidado, Consolidado datosActualizar);

    public ApiResponse<ConsolidadoDTO> generarInformacionGeneral(Integer idEvaluado, Integer idPeriodoAcademico);

    public ApiResponse<ActividadPaginadaDTO> filtrarActividadesPaginadas(Integer idEvaluado, Integer idPeriodoAcademico,
            String nombreActividad, String idTipoActividad, String idTipoFuente, String idEstadoFuente, Pageable pageable);

    public ApiResponse<ConsolidadoArchivoDTO> aprobarConsolidado(Integer idEvaluado, Integer idEvaluador, Integer idPeriodoAcademico, String nota);

    ApiResponse<List<InformacionConsolidadoDTO>> obtenerTodos();

    ApiResponse<Page<HistoricoCalificacionesDTO>> obtenerHistoricoCalificaciones(
        List<Integer> periodos, Integer idUsuario, String nombre, String identificacion,
        String facultad, String departamento, String categoria, Pageable pageable);

    ByteArrayResource generarExcel(Boolean ascendingOrder, Integer idPeriodoAcademico, Integer idUsuario, String nombre, String identificacion, String facultad, String departamento, String categoria) throws IOException;

    public ByteArrayResource generarExcelHistorico(List<Integer> periodos, Integer idUsuario, String nombre, String identificacion, String facultad, String departamento, String categoria) throws IOException;
}
