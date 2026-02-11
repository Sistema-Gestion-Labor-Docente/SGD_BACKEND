package co.edu.unicauca.sgd.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.sgd.api.domain.ConfiguracionGeneral;

public interface ConfiguracionGeneralRepository extends JpaRepository<ConfiguracionGeneral, Integer> {
    Optional<ConfiguracionGeneral> findByClave(String clave);
}
