package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.EstadoConsolidado;

/**
 * Repositorio para la entidad EstadoConsolidado.
 */
@Repository
public interface EstadoConsolidadoRepository extends JpaRepository<EstadoConsolidado, Integer> {
}
