package co.edu.unicauca.sed.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.ActividadDate;

/**
 * Repositorio para la entidad ActividadDate.
 */
@Repository
public interface ActividadDateRepository extends JpaRepository<ActividadDate, Integer> {
    void deleteByActividad(Actividad actividad);
    List<ActividadDate> findByActividad(Actividad actividad);
}
