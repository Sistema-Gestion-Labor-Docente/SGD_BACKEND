package co.edu.unicauca.sed.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.unicauca.sed.api.domain.Consolidado;
import co.edu.unicauca.sed.api.domain.Proceso;

public interface ConsolidadoRepository extends JpaRepository<Consolidado, Integer>, JpaSpecificationExecutor<Consolidado> {
  Optional<Consolidado> findByProceso(Proceso proceso);
}
