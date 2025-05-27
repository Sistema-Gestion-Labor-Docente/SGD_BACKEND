package co.edu.unicauca.sed.api.service.actividad;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.EstadoActividad;
import co.edu.unicauca.sed.api.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * Interfaz para definir los métodos del servicio de gestión de Estados de Actividad.
 */
public interface EstadoActividadService {

    /**
     * Recupera todos los estados de actividad con soporte de paginación.
     *
     * @param pageable Configuración de la paginación.
     * @return ApiResponse con la página de estados de actividad disponibles.
     */
    ResponseEntity<ApiResponse<Page<EstadoActividad>>> obtenerTodos(Pageable pageable);

    /**
     * Busca un estado de actividad por su identificador único (OID).
     *
     * @param oid El identificador del estado de actividad.
     * @return ApiResponse con el estado de actividad si es encontrado.
     */
    ResponseEntity<ApiResponse<EstadoActividad>> buscarPorOid(Integer oid);

    /**
     * Guarda un nuevo estado de actividad en la base de datos.
     *
     * @param estadoActividad El objeto EstadoActividad que se desea guardar.
     * @return ApiResponse con el objeto guardado o un mensaje de error.
     */
    ResponseEntity<ApiResponse<EstadoActividad>> guardar(EstadoActividad estadoActividad);

    /**
     * Actualiza un estado de actividad existente.
     *
     * @param oid             El identificador del estado de actividad a actualizar.
     * @param estadoActividad Datos actualizados del estado de actividad.
     * @return ApiResponse con el objeto actualizado o un mensaje de error.
     */
    ResponseEntity<ApiResponse<EstadoActividad>> actualizar(Integer oid, EstadoActividad estadoActividad);

    /**
     * Elimina un estado de actividad por su identificador único (OID).
     *
     * @param oid El identificador del estado de actividad que se desea eliminar.
     * @return ApiResponse con el resultado de la eliminación.
     */
    ResponseEntity<ApiResponse<Void>> eliminar(Integer oid);

    /**
     * Asigna un estado de actividad a una actividad.
     *
     * @param actividad          La actividad a la que se asignará el estado.
     * @param oidEstadoActividad El identificador del estado de actividad.
     * @return ApiResponse con el resultado de la asignación.
     */
    ResponseEntity<ApiResponse<Void>> asignarEstadoActividad(Actividad actividad, Integer oidEstadoActividad);
}
