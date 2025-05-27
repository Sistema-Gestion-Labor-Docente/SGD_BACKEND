package co.edu.unicauca.sed.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.ActividadInt;

/**
 * Repositorio para la entidad ActividadInt.
 */
@Repository
public interface ActividadIntRepository extends JpaRepository<ActividadInt, Integer> {
    void deleteByActividad(Actividad actividad);
    List<ActividadInt> findByActividad(Actividad actividad);
}
