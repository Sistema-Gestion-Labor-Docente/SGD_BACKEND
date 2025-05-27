package co.edu.unicauca.sed.api.service.evaluacion_docente;

import co.edu.unicauca.sed.api.domain.Pregunta;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz para la gestión de preguntas en el sistema.
 */
@Service
public interface PreguntaService {

    /**
     * Obtiene todas las preguntas registradas.
     *
     * @return ApiResponse con la lista de preguntas.
     */
    ApiResponse<Page<Pregunta>> obtenerTodos(Pageable pageable);

    /**
     * Busca una pregunta por su identificador único (OID).
     *
     * @param oid Identificador de la pregunta.
     * @return ApiResponse con la pregunta encontrada.
     */
    ApiResponse<Pregunta> buscarPorOid(Integer oid);

    /**
     * Guarda una nueva pregunta en la base de datos.
     *
     * @param pregunta Datos de la pregunta a guardar.
     * @return ApiResponse con la pregunta guardada.
     */
    ApiResponse<Pregunta> guardar(Pregunta pregunta);

    /**
     * Guarda una lista de preguntas en la base de datos.
     *
     * @param preguntas Lista de preguntas a guardar.
     * @return ApiResponse con la lista de preguntas guardadas.
     */
    ApiResponse<List<Pregunta>> guardarTodas(List<Pregunta> preguntas);

    /**
     * Elimina una pregunta por su identificador.
     *
     * @param oid Identificador de la pregunta a eliminar.
     * @return ApiResponse indicando el resultado de la eliminación.
     */
    ApiResponse<Void> eliminar(Integer oid);
}
