package co.edu.unicauca.sed.api.repository;

import co.edu.unicauca.sed.api.domain.Autoevaluacion;
import co.edu.unicauca.sed.api.domain.OportunidadMejora;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OportunidadMejoraRepository extends JpaRepository<OportunidadMejora, Integer>, JpaSpecificationExecutor<OportunidadMejora> {
    List<OportunidadMejora> findByAutoevaluacion(Autoevaluacion autoevaluacion);
}
