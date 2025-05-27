package co.edu.unicauca.sed.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.edu.unicauca.sed.api.domain.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    @Query("SELECT DISTINCT r.nombre FROM Rol r")
    List<String> findDistinctNombre();
}
