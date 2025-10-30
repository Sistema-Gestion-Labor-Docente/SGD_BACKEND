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
import co.edu.unicauca.sgd.api.repository.projection.UsuarioHorasProjection;

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

    List<UsuarioActividadCalendario> findByActividadCalendario_Actividad_OidActividadIn(List<Integer> oidActividades);

    List<UsuarioActividadCalendario> findByUsuario_OidUsuarioAndActividadCalendario_CargoActividad_OidCargoActividad(Integer oidUsuario, Integer oidCargoActividad);

    List<UsuarioActividadCalendario> findByUsuario_OidUsuarioAndActividadCalendario_Actividad_TipoActividad_OidTipoActividad(Integer oidUsuario, Integer oidTipoActividad);

    @Query(
      value = """
        SELECT DISTINCT a.OIDACTIVIDAD
        FROM USUARIOACTIVIDADCALENDARIO uac
        JOIN ACTIVIDADCALENDARIO ac ON uac.OIDACTIVIDADCALENDARIO = ac.OIDACTIVIDADCALENDARIO
        JOIN ACTIVIDAD a ON ac.OIDACTIVIDAD = a.OIDACTIVIDAD
        JOIN USUARIO u ON uac.OIDUSUARIO = u.OIDUSUARIO
        JOIN USUARIODEPARTAMENTO ud ON ud.OIDUSUARIO = u.OIDUSUARIO
        WHERE ac.OIDCALENDARIO = :oidCalendario
          AND ( :oidTipoActividad IS NULL OR a.OIDTIPOACTIVIDAD = :oidTipoActividad )
          AND ud.OIDDEPARTAMENTO = :oidDepartamento
      """,
      countQuery = """
        SELECT COUNT(DISTINCT a.OIDACTIVIDAD)
        FROM USUARIOACTIVIDADCALENDARIO uac
        JOIN ACTIVIDADCALENDARIO ac ON uac.OIDACTIVIDADCALENDARIO = ac.OIDACTIVIDADCALENDARIO
        JOIN ACTIVIDAD a ON ac.OIDACTIVIDAD = a.OIDACTIVIDAD
        JOIN USUARIO u ON uac.OIDUSUARIO = u.OIDUSUARIO
        JOIN USUARIODEPARTAMENTO ud ON ud.OIDUSUARIO = u.OIDUSUARIO
        WHERE ac.OIDCALENDARIO = :oidCalendario
          AND ( :oidTipoActividad IS NULL OR a.OIDTIPOACTIVIDAD = :oidTipoActividad )
          AND ud.OIDDEPARTAMENTO = :oidDepartamento
      """,
      nativeQuery = true
    )
    Page<Integer> findDistinctActividadIdsByFilters(
        @Param("oidCalendario") Integer oidCalendario,
        @Param("oidDepartamento") Integer oidDepartamento,
        @Param("oidTipoActividad") Integer oidTipoActividad,
        Pageable pageable);

    @Query("""
        SELECT uac.usuario.oidUsuario AS oidUsuario,
               COALESCE(SUM(COALESCE(a.horas, 0)), 0) AS totalHoras
        FROM UsuarioActividadCalendario uac
        JOIN uac.actividadCalendario ac
        JOIN ac.actividad a
        WHERE uac.usuario.oidUsuario IN :oidUsuarios
        GROUP BY uac.usuario.oidUsuario
    """)
    List<UsuarioHorasProjection> sumarHorasPorUsuarios(@Param("oidUsuarios") List<Integer> oidUsuarios);

    @Query("""
        SELECT COALESCE(SUM(COALESCE(a.horas, 0)), 0)
        FROM UsuarioActividadCalendario uac
        JOIN uac.actividadCalendario ac
        JOIN ac.actividad a
        WHERE uac.usuario.oidUsuario = :oidUsuario
    """)
    Float sumarHorasPorUsuario(@Param("oidUsuario") Integer oidUsuario);

}
