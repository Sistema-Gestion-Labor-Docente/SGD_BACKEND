package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.ObjetivoComponente;

@Repository
public interface ObjetivoComponenteRepository extends JpaRepository<ObjetivoComponente, Integer> {
}
