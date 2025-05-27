package co.edu.unicauca.sed.api.service.periodo_academico;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import co.edu.unicauca.sed.api.domain.PeriodoAcademico;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import co.edu.unicauca.sed.api.dto.PeriodoExternoDTO;

@Service
public interface PeriodoAcademicoService {

    /**
     * Obtiene una lista paginada de todos los períodos académicos.
     *
     * @param pageable Configuración de la paginación.
     * @return ApiResponse con la lista de períodos académicos.
     */
    ApiResponse<Page<PeriodoAcademico>> obtenerTodos(Pageable pageable);

    /**
     * Busca un período académico por su identificador único (OID).
     *
     * @param oid Identificador del período académico.
     * @return ApiResponse con el período académico encontrado.
     */
    ApiResponse<PeriodoAcademico> buscarPorId(Integer oid);

    /**
     * Guarda un nuevo período académico en la base de datos.
     *
     * @param periodoAcademico Datos del período académico a guardar.
     * @return ApiResponse con el período académico guardado.
     */
    ApiResponse<PeriodoAcademico> guardar(PeriodoAcademico periodoAcademico);

    /**
     * Actualiza un período académico existente.
     *
     * @param oid              Identificador del período académico a actualizar.
     * @param periodoAcademico Datos actualizados del período académico.
     * @return ApiResponse indicando el resultado de la actualización.
     */
    ApiResponse<Void> actualizar(Integer oid, PeriodoAcademico periodoAcademico);

    /**
     * Elimina un período académico por su identificador.
     *
     * @param oid Identificador del período académico a eliminar.
     * @return ApiResponse indicando el resultado de la eliminación.
     */
    ApiResponse<Void> eliminar(Integer oid);

    /**
     * Obtiene el período académico que está marcado como activo en la base de
     * datos.
     *
     * @return ApiResponse con el período académico activo si existe.
     */
    ApiResponse<PeriodoAcademico> obtenerPeriodoAcademicoActivo();

    /**
     * Obtiene el identificador del período académico activo.
     *
     * @return El identificador del período académico activo.
     * @throws IllegalStateException Si no se encuentra un período académico activo.
     */
    Integer obtenerIdPeriodoAcademicoActivo();

    ApiResponse<List<PeriodoExternoDTO>> obtenerPeriodosNoRegistrados();
}
