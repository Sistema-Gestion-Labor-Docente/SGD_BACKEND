package co.edu.unicauca.sgd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.Autoevaluacion;
import co.edu.unicauca.sgd.api.domain.OportunidadMejora;

@Repository
public interface OportunidadMejoraRepository extends JpaRepository<OportunidadMejora, Integer>, JpaSpecificationExecutor<OportunidadMejora> {
    List<OportunidadMejora> findByAutoevaluacion(Autoevaluacion autoevaluacion);
}
