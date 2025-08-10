package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.unicauca.sgd.api.domain.Materia;

public interface MateriaRepository extends JpaRepository<Materia, Integer>, JpaSpecificationExecutor<Materia> {

}
