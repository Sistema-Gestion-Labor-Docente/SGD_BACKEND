package co.edu.unicauca.sgd.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.Autoevaluacion;
import co.edu.unicauca.sgd.api.domain.AutoevaluacionOds;

@Repository
public interface AutoevaluacionOdsRepository extends JpaRepository<AutoevaluacionOds, Integer>, JpaSpecificationExecutor<AutoevaluacionOds> {
    List<AutoevaluacionOds> findByAutoevaluacion(Autoevaluacion autoevaluacion);

    @Query("SELECT MAX(a.oidAutoevaluacionOds) FROM AutoevaluacionOds a")
    Integer obtenerMaxOidOds();
}
