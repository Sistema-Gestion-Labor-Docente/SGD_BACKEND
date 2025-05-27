package co.edu.unicauca.sgd.api.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.EstadoFuente;

@Repository
public interface EstadoFuenteRepository extends JpaRepository<EstadoFuente, Integer> {
    Optional<EstadoFuente> findByNombreEstado(String nombreEstado);
}
