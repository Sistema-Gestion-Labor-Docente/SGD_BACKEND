package co.edu.unicauca.sed.api.repository;

import co.edu.unicauca.sed.api.domain.Componente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComponenteRepository extends JpaRepository<Componente, Integer> {
}
