package co.edu.unicauca.sgd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;

public interface UsuarioDepartamentoRepository extends JpaRepository<UsuarioDepartamento, Integer>, JpaSpecificationExecutor<UsuarioDepartamento> {

    List<UsuarioDepartamento> findByDepartamento(Departamento departamento);

    boolean existsByUsuarioOidUsuarioAndDepartamentoOidDepartamento(Integer oidUsuario, Integer oidDepartamento);

    @Query("""
        SELECT DISTINCT ud
        FROM UsuarioDepartamento ud
        JOIN ud.usuario u
        JOIN UsuarioActividadCalendario uac ON uac.usuario = u
        JOIN uac.actividadCalendario ac
        JOIN ac.actividad act
        JOIN act.tipoActividad tipo
        WHERE UPPER(tipo.nombre) = UPPER(:nombreTipoActividad)
          AND ud.departamento.oidDepartamento = :oidDepartamento
    """)
    List<UsuarioDepartamento> findProfesoresConTipoActividad(@Param("nombreTipoActividad") String nombreTipoActividad,
            @Param("oidDepartamento") Integer oidDepartamento);

    @Query("""
        SELECT DISTINCT ud
        FROM UsuarioDepartamento ud
        JOIN ud.usuario u
        JOIN UsuarioActividadCalendario uac ON uac.usuario = u
        JOIN uac.actividadCalendario ac
        JOIN ac.actividad act
        JOIN act.tipoActividad tipo
        WHERE UPPER(tipo.nombre) <> UPPER(:nombreTipoActividad)
          AND ud.departamento.oidDepartamento = :oidDepartamento
    """)
    List<UsuarioDepartamento> findProfesoresConTipoActividadDiferente(@Param("nombreTipoActividad") String nombreTipoActividad,
            @Param("oidDepartamento") Integer oidDepartamento);

}
