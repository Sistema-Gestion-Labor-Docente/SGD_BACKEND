package co.edu.unicauca.sed.api.repository;

import co.edu.unicauca.sed.api.domain.Fuente;
import co.edu.unicauca.sed.api.domain.InformeAdministracion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InformeAdministracionRepository extends JpaRepository<InformeAdministracion, Integer> {
    void deleteAllByFuente(Fuente fuente);
    List<InformeAdministracion> findAllByFuente(Fuente fuente);
}
