package co.edu.unicauca.sed.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.TipoActividad;

@Repository
public interface TipoActividadRepository extends JpaRepository<TipoActividad, Integer> {
    @Query("SELECT DISTINCT t.nombre FROM TipoActividad t")
    List<String> findDistinctNombre();
}
