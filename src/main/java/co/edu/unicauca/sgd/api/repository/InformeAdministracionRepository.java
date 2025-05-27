package co.edu.unicauca.sgd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.Fuente;
import co.edu.unicauca.sgd.api.domain.InformeAdministracion;

@Repository
public interface InformeAdministracionRepository extends JpaRepository<InformeAdministracion, Integer> {
    void deleteAllByFuente(Fuente fuente);
    List<InformeAdministracion> findAllByFuente(Fuente fuente);
}
