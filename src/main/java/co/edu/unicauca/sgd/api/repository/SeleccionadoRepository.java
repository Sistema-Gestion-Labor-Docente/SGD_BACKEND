package co.edu.unicauca.sgd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.sgd.api.domain.Seleccionado;

public interface SeleccionadoRepository extends JpaRepository<Seleccionado, Integer>, JpaSpecificationExecutor<Seleccionado> {

    List<Seleccionado> findByCalendarioOidcalendario(Integer oidCalendario);

    List<Seleccionado> findByUsuarioOidUsuario(Integer oidUsuario);

    boolean existsByCalendarioOidcalendarioAndUsuarioOidUsuario(Integer oidCalendario, Integer oidUsuario);

    void deleteByCalendarioOidcalendario(Integer oidCalendario);

    @Query("SELECT s " +
       "FROM Seleccionado s, UsuarioDepartamento ud " +
       "WHERE ud.usuario = s.usuario " +                       // usa la relación entidad->entidad
       "  AND s.calendario.oidcalendario = :oidCalendario " +
       "  AND ud.departamento.oidDepartamento = :oidDepartamento") // usa la propiedad Java correcta del Departamento
     List<Seleccionado> findByCalendarioAndDepartamento(@Param("oidCalendario") Integer oidCalendario,
                                                       @Param("oidDepartamento") Integer oidDepartamento);
}
