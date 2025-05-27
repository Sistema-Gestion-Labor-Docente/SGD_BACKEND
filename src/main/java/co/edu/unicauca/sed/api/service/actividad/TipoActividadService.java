package co.edu.unicauca.sed.api.service.actividad;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import co.edu.unicauca.sed.api.domain.TipoActividad;
import co.edu.unicauca.sed.api.dto.ApiResponse;

/**
 * Interfaz para definir los métodos del servicio de gestión de Tipos de
 * Actividad.
 */
public interface TipoActividadService {

    /**
     * Recupera todas las actividades disponibles con soporte de paginación.
     *
     * @param pageable Configuración de la paginación.
     * @return Página de actividades disponibles.
     */
    ApiResponse<Page<TipoActividad>> listarTodos(Pageable pageable);

    /**
     * Busca un tipo de actividad por su identificador único (OID).
     *
     * @param oid El identificador de la actividad.
     * @return La actividad si es encontrada, o null si no existe.
     */
    TipoActividad buscarPorOid(Integer oid);

    /**
     * Guarda un nuevo tipo de actividad en la base de datos.
     *
     * @param tipoActividad El objeto TipoActividad que se desea guardar.
     * @return ApiResponse con el objeto guardado o un mensaje de error.
     */
    ApiResponse<TipoActividad> guardar(TipoActividad tipoActividad);

    /**
     * Actualiza un tipo de actividad existente.
     *
     * @param oid           El identificador del tipo de actividad a actualizar.
     * @param tipoActividad Datos actualizados del tipo de actividad.
     * @return El objeto actualizado si existe, o lanza una excepción si no se
     *         encuentra.
     */
    TipoActividad actualizar(Integer oid, TipoActividad tipoActividad);

    /**
     * Elimina un tipo de actividad por su identificador único (OID).
     *
     * @param oid El identificador de la actividad que se desea eliminar.
     */
    void eliminar(Integer oid);
}
