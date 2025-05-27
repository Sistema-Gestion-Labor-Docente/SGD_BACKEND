package co.edu.unicauca.sed.api.repository;

import co.edu.unicauca.sed.api.domain.Autoevaluacion;
import co.edu.unicauca.sed.api.domain.LeccionAprendida;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LeccionAprendidaRepository extends JpaRepository<LeccionAprendida, Integer>, JpaSpecificationExecutor<LeccionAprendida> {
    List<LeccionAprendida> findByAutoevaluacion(Autoevaluacion autoevaluacion);
}
