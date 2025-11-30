package co.edu.unicauca.sgd.api.repository.projection;

/**
 * Representa la suma de horas asociadas a un usuario,
 * agrupadas por tipo de actividad.
 */
public interface UsuarioHorasPorTipoActividadProjection {

    Integer getOidUsuario();

    Integer getOidTipoActividad();

    String getNombreTipoActividad();

    Float getTotalHoras();
}

