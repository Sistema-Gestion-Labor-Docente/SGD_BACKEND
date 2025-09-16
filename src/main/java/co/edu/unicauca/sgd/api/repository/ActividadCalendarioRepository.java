package co.edu.unicauca.sgd.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.ActividadCalendario;

@Repository
public interface ActividadCalendarioRepository extends JpaRepository<ActividadCalendario, Integer>, JpaSpecificationExecutor<ActividadCalendario> {
    Optional<ActividadCalendario> findByActividad_OidActividadAndCalendario_Oidcalendario(Integer oidActividad, Integer oidCalendario);
    List<ActividadCalendario> findByActividad_OidActividad(Integer oidActividad);

}
