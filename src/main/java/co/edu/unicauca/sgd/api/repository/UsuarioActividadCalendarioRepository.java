package co.edu.unicauca.sgd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;

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
}
