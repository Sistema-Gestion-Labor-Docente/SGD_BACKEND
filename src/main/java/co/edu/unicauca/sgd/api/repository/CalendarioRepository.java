package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.Calendario;

@Repository
public interface CalendarioRepository extends JpaRepository<Calendario, Integer>, JpaSpecificationExecutor<Calendario> {

}

