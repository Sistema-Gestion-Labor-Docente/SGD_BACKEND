package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.unicauca.sgd.api.domain.Departamento;

public interface DepartamentoRepository extends JpaRepository<Departamento, Integer>, JpaSpecificationExecutor<Departamento> {

}
