package co.edu.unicauca.sed.api.repository;

import co.edu.unicauca.sed.api.domain.ObjetivoComponente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjetivoComponenteRepository extends JpaRepository<ObjetivoComponente, Integer> {
}
