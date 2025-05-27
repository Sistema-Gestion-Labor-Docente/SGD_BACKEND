package co.edu.unicauca.sed.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.EstadoEtapaDesarrollo;

/**
 * Repositorio para la entidad EstadoEtapaDesarrollo.
 */
@Repository
public interface EstadoEtapaDesarrolloRepository extends JpaRepository<EstadoEtapaDesarrollo, Integer> {
}
