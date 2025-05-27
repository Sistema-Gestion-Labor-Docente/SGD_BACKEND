package co.edu.unicauca.sed.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import co.edu.unicauca.sed.api.domain.Autoevaluacion;
import co.edu.unicauca.sed.api.domain.Fuente;

@Repository
public interface AutoevaluacionRepository extends JpaRepository<Autoevaluacion, Integer>, JpaSpecificationExecutor<Autoevaluacion> {
    Optional<Autoevaluacion> findByFuente(Fuente fuente);
}