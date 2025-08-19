package co.edu.unicauca.sgd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.unicauca.sgd.api.domain.Fecha;

public interface FechaRepository extends JpaRepository<Fecha, Integer>, JpaSpecificationExecutor<Fecha> {

    List<Fecha> findByCalendario_Oidcalendario(Integer oidCalendario);

}

