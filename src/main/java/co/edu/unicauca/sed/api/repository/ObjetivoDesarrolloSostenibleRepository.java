package co.edu.unicauca.sed.api.repository;

import co.edu.unicauca.sed.api.domain.ObjetivoDesarrolloSostenible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjetivoDesarrolloSostenibleRepository extends JpaRepository<ObjetivoDesarrolloSostenible, Integer>, JpaSpecificationExecutor<ObjetivoDesarrolloSostenible> {

}
