package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.ObjetivoDesarrolloSostenible;

@Repository
public interface ObjetivoDesarrolloSostenibleRepository extends JpaRepository<ObjetivoDesarrolloSostenible, Integer>, JpaSpecificationExecutor<ObjetivoDesarrolloSostenible> {

}
