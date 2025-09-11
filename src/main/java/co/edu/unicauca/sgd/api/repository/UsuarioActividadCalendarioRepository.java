package co.edu.unicauca.sgd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.UsuarioActividadCalendario;

@Repository
public interface UsuarioActividadCalendarioRepository extends JpaRepository<UsuarioActividadCalendario, Integer> {

    // Buscar todas por Actividad
    List<UsuarioActividadCalendario> findByActividad_OidActividad(Integer oidActividad);

    // Buscar todas por Calendario
    List<UsuarioActividadCalendario> findByCalendario_Oidcalendario(Integer oidCalendario);

    // Buscar por usuario
    List<UsuarioActividadCalendario> findByUsuario_OidUsuario(Integer oidUsuario);

    // Buscar por usuario y calendario
    List<UsuarioActividadCalendario> findByUsuario_OidUsuarioAndCalendario_Oidcalendario(Integer oidUsuario, Integer oidCalendario);

    // Eliminar solo la relación usuario-actividad-calendario (no la actividad)
    void deleteByActividad_OidActividadAndUsuario_OidUsuarioAndCalendario_Oidcalendario(Integer oidActividad, Integer oidUsuario, Integer oidCalendario);

    boolean existsByActividad_OidActividadAndUsuario_OidUsuarioAndCalendario_Oidcalendario(Integer oidActividad, Integer oidUsuario, Integer oidCalendario);

}
