package co.edu.unicauca.sed.api.service.periodo_academico;

import co.edu.unicauca.sed.api.domain.EstadoPeriodoAcademico;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface EstadoPeriodoAcademicoService {

    /**
     * Guarda un nuevo estado de período académico en la base de datos.
     *
     * @param estadoPeriodoAcademico Datos del estado a guardar.
     * @return ApiResponse con el estado de período académico guardado.
     */
    ApiResponse<EstadoPeriodoAcademico> guardar(EstadoPeriodoAcademico estadoPeriodoAcademico);

    /**
     * Busca un estado de período académico por su identificador único (ID).
     *
     * @param id Identificador del estado de período académico.
     * @return ApiResponse con el estado de período académico encontrado.
     */
    ApiResponse<EstadoPeriodoAcademico> buscarPorId(Integer id);

    /**
     * Obtiene una lista paginada de todos los estados de períodos académicos.
     *
     * @param pageable Configuración de la paginación.
     * @return ApiResponse con la lista de estados de períodos académicos.
     */
    ApiResponse<Page<EstadoPeriodoAcademico>> buscarTodos(Pageable pageable);

    /**
     * Actualiza un estado de período académico existente.
     *
     * @param id                     Identificador del estado a actualizar.
     * @param estadoPeriodoAcademico Datos actualizados del estado.
     * @return ApiResponse con el estado de período académico actualizado.
     */
    ApiResponse<EstadoPeriodoAcademico> actualizar(Integer id, EstadoPeriodoAcademico estadoPeriodoAcademico);

    /**
     * Elimina un estado de período académico por su identificador.
     *
     * @param id Identificador del estado de período académico a eliminar.
     * @return ApiResponse indicando el resultado de la eliminación.
     */
    ApiResponse<Void> eliminar(Integer id);
}
