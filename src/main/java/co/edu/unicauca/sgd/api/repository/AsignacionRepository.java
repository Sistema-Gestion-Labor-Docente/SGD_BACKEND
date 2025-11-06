package co.edu.unicauca.sgd.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.sgd.api.domain.Asignacion;

public interface AsignacionRepository extends JpaRepository<Asignacion, Integer> {

    List<Asignacion> findByNecesidad_OidNecesidad(Integer oidNecesidad);

    long countByNecesidad_OidNecesidad(Integer oidNecesidad);

    Optional<Asignacion> findByNecesidad_OidNecesidadAndSeleccionado_OidSeleccionado(Integer oidNecesidad, Integer oidSeleccionado);
}
