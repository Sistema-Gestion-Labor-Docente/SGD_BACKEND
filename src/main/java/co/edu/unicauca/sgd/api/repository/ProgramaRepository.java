package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.unicauca.sgd.api.domain.Programa;

public interface ProgramaRepository extends JpaRepository<Programa, Integer>, JpaSpecificationExecutor<Programa> {

}
