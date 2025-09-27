package co.edu.unicauca.sgd.api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;
import co.edu.unicauca.sgd.api.repository.projection.ActividadUsuariosProjection;

@Repository
public interface UsuarioActividadCalendarioRepository extends JpaRepository<UsuarioActividadCalendario, Integer>, JpaSpecificationExecutor<UsuarioActividadCalendario> {

    // Buscar todas por Actividad (todas las relaciones de usuario para cualquier actividadCalendario de la actividad)
    List<UsuarioActividadCalendario> findByActividadCalendario_Actividad_OidActividad(Integer oidActividad);

    // Buscar por ActividadCalendario específico
    List<UsuarioActividadCalendario> findByActividadCalendario_OidActividadCalendario(Integer oidActividadCalendario);

    // Buscar todas por Calendario
    List<UsuarioActividadCalendario> findByActividadCalendario_Calendario_Oidcalendario(Integer oidCalendario);

    // Buscar por usuario
    List<UsuarioActividadCalendario> findByUsuario_OidUsuario(Integer oidUsuario);

    // Buscar por usuario y calendario (usa actividadCalendario -> calendario)
    List<UsuarioActividadCalendario> findByUsuario_OidUsuarioAndActividadCalendario_Calendario_Oidcalendario(Integer oidUsuario, Integer oidCalendario);

    // Eliminar relaciones por actividadCalendario y usuario
    void deleteByActividadCalendario_OidActividadCalendarioAndUsuario_OidUsuario(Integer oidActividadCalendario, Integer oidUsuario);

    // Eliminar por actividadCalendario (varias relaciones)
    void deleteByActividadCalendario_OidActividadCalendario(Integer oidActividadCalendario);

    boolean existsByActividadCalendario_OidActividadCalendarioAndUsuario_OidUsuario(Integer oidActividadCalendario, Integer oidUsuario);

    boolean existsByActividadCalendario_Actividad_OidActividadAndUsuario_OidUsuarioAndActividadCalendario_Calendario_Oidcalendario(Integer oidActividad, Integer oidUsuario, Integer oidCalendario);

    @Query(
      value = """
        SELECT
          a.OIDACTIVIDAD                     AS oidActividad,
          ac.OIDACTIVIDADCALENDARIO          AS oidActividadCalendario,
          ac.OIDCARGOACTIVIDAD               AS oidCargoActividad,
          ac.OIDCALENDARIO                   AS oidCalendario,
          a.NOMBREACTIVIDAD                  AS nombreActividad,
          a.HORAS                            AS horas,
          a.SEMANAS                          AS semanas,
          a.IDLABORDOCENTE                   AS idLaborDocente,
          a.INFORMEEJECUTIVO                 AS informeEjecutivo,
          -- agregamos usuarios que pertenecen al departamento (como JSON array)
          json_agg(
            json_build_object(
              'oidUsuario', u.OIDUSUARIO,
              'identificacion', u.IDENTIFICACION,
              'nombres', u.NOMBRES,
              'apellidos', u.APELLIDOS
            )
            ORDER BY u.OIDUSUARIO
          ) FILTER (WHERE u.OIDUSUARIO IS NOT NULL) AS usuariosJson
        FROM USUARIOACTIVIDADCALENDARIO uac
        JOIN ACTIVIDADCALENDARIO ac ON uac.OIDACTIVIDADCALENDARIO = ac.OIDACTIVIDADCALENDARIO
        JOIN ACTIVIDAD a ON ac.OIDACTIVIDAD = a.OIDACTIVIDAD
        JOIN USUARIO u ON uac.OIDUSUARIO = u.OIDUSUARIO
        JOIN USUARIODEPARTAMENTO ud ON ud.OIDUSUARIO = u.OIDUSUARIO
        WHERE ac.OIDCALENDARIO = :oidCalendario
          AND a.OIDTIPOACTIVIDAD = :oidTipoActividad
          AND ud.OIDDEPARTAMENTO = :oidDepartamento
        GROUP BY a.OIDACTIVIDAD, ac.OIDACTIVIDADCALENDARIO, ac.OIDCARGOACTIVIDAD, ac.OIDCALENDARIO,
                 a.NOMBREACTIVIDAD, a.HORAS, a.SEMANAS, a.IDLABORDOCENTE, a.INFORMEEJECUTIVO
      """,
      countQuery = """
        SELECT COUNT(DISTINCT a.OIDACTIVIDAD)
        FROM USUARIOACTIVIDADCALENDARIO uac
        JOIN ACTIVIDADCALENDARIO ac ON uac.OIDACTIVIDADCALENDARIO = ac.OIDACTIVIDADCALENDARIO
        JOIN ACTIVIDAD a ON ac.OIDACTIVIDAD = a.OIDACTIVIDAD
        JOIN USUARIO u ON uac.OIDUSUARIO = u.OIDUSUARIO
        JOIN USUARIODEPARTAMENTO ud ON ud.OIDUSUARIO = u.OIDUSUARIO
        WHERE ac.OIDCALENDARIO = :oidCalendario
          AND a.OIDTIPOACTIVIDAD = :oidTipoActividad
          AND ud.OIDDEPARTAMENTO = :oidDepartamento
      """,
      nativeQuery = true
    )
    Page<ActividadUsuariosProjection> findActividadesWithUsersByFilters(
        @Param("oidCalendario") Integer oidCalendario,
        @Param("oidDepartamento") Integer oidDepartamento,
        @Param("oidTipoActividad") Integer oidTipoActividad,
        Pageable pageable);


}
