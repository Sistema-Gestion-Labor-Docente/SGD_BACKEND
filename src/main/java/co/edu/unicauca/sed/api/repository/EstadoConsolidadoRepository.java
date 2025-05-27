package co.edu.unicauca.sed.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.EstadoConsolidado;

/**
 * Repositorio para la entidad EstadoConsolidado.
 */
@Repository
public interface EstadoConsolidadoRepository extends JpaRepository<EstadoConsolidado, Integer> {
}
