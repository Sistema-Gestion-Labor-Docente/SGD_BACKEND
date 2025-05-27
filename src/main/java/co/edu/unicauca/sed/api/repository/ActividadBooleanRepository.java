package co.edu.unicauca.sed.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.ActividadBoolean;

/**
 * Repositorio para la entidad ActividadBoolean.
 */
@Repository
public interface ActividadBooleanRepository extends JpaRepository<ActividadBoolean, Integer> {
    void deleteByActividad(Actividad actividad);
    List<ActividadBoolean> findByActividad(Actividad actividad);
}
