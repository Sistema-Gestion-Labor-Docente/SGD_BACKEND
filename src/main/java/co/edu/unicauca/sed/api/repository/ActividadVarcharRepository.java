package co.edu.unicauca.sed.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.Actividad;
import co.edu.unicauca.sed.api.domain.ActividadVarchar;

/**
 * Repositorio para la entidad ActividadVarchar.
 */
@Repository
public interface ActividadVarcharRepository extends JpaRepository<ActividadVarchar, Integer> {
    void deleteByActividad(Actividad actividad);
    List<ActividadVarchar> findByActividad(Actividad actividad);
}
