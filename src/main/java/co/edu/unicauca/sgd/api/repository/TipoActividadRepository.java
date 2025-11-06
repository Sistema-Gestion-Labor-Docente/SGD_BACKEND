package co.edu.unicauca.sgd.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.TipoActividad;

@Repository
public interface TipoActividadRepository extends JpaRepository<TipoActividad, Integer> {
    @Query("SELECT DISTINCT t.nombre FROM TipoActividad t")
    List<String> findDistinctNombre();

    Optional<TipoActividad> findByNombreIgnoreCase(String nombre);
}
