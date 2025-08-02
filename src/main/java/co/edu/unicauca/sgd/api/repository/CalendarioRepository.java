package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.unicauca.sgd.api.domain.Calendario;

public interface CalendarioRepository extends JpaRepository<Calendario, Integer>, JpaSpecificationExecutor<Calendario> {

}

