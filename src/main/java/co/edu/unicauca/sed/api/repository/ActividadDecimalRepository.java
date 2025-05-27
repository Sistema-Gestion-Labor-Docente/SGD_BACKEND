package co.edu.unicauca.sed.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.ActividadDecimal;

/**
 * Repositorio para la entidad ActividadDecimal.
 */
@Repository
public interface ActividadDecimalRepository extends JpaRepository<ActividadDecimal, Integer> {
    void deleteByActividad(Actividad actividad);
    List<ActividadDecimal> findByActividad(Actividad actividad);
}
