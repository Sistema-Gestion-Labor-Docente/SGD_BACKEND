package co.edu.unicauca.sgd.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.Programa;

@Repository
public interface ProgramaRepository extends JpaRepository<Programa, Integer>, JpaSpecificationExecutor<Programa> {

    Optional<Programa> findByCoordinador_OidUsuario(Integer oidUsuario);

}
