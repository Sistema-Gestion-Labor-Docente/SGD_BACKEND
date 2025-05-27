package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.Componente;

@Repository
public interface ComponenteRepository extends JpaRepository<Componente, Integer> {
}
