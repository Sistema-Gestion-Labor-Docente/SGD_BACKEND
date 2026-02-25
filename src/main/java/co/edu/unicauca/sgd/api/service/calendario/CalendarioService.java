package co.edu.unicauca.sgd.api.service.calendario;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTORequest;
import co.edu.unicauca.sgd.api.dto.calendario.CalendarioDTOResponse;

/**
 * Servicio de negocio para la gestion de calendarios.
 */
public interface CalendarioService {

    /**
     * Lista calendarios con filtros opcionales y paginacion.
     *
     * @param anioCalendario anio del calendario (opcional)
     * @param numeroCalendario numero del calendario (opcional)
     * @param estado estado del calendario (opcional)
     * @param pageable configuracion de pagina y orden
     * @return respuesta con pagina de calendarios
     */
    ApiResponse<Page<CalendarioDTOResponse>> obtenerTodos(String anioCalendario,
                                                          Integer numeroCalendario,
                                                          String estado,
                                                          Pageable pageable);

    /**
     * Consulta un calendario por su identificador.
     *
     * @param oid identificador del calendario
     * @return respuesta con el calendario
     */
    ApiResponse<CalendarioDTOResponse> buscarPorId(Integer oid);

    /**
     * Crea un calendario.
     *
     * @param dto datos del calendario
     * @return respuesta con el calendario creado
     */
    ApiResponse<CalendarioDTOResponse> guardar(CalendarioDTORequest dto);

    /**
     * Actualiza un calendario existente.
     *
     * @param id identificador del calendario a actualizar
     * @param dto datos a actualizar
     * @return respuesta con el calendario actualizado
     */
    ApiResponse<CalendarioDTOResponse> actualizar(Integer id, CalendarioDTORequest dto);

    /**
     * Elimina un calendario por su identificador.
     *
     * @param oid identificador del calendario
     * @return respuesta vacia con el resultado de la operacion
     */
    ApiResponse<Void> eliminar(Integer oid);

    /**
     * Genera el PDF del calendario.
     *
     * @param oidCalendario identificador del calendario
     * @return PDF en memoria
     * @throws IOException si ocurre un error al generar el PDF
     */
    ByteArrayOutputStream generarCalendarioPdf(Integer oidCalendario) throws IOException;
}

