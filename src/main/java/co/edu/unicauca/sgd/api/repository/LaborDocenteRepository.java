package co.edu.unicauca.sgd.api.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.LaborDocente;
import co.edu.unicauca.sgd.api.domain.PeriodoAcademico;
import co.edu.unicauca.sgd.api.domain.Usuario;

@Repository
public interface LaborDocenteRepository extends JpaRepository<LaborDocente, Integer> {
    Optional<LaborDocente> findByUsuarioOidUsuario(Integer oidUsuario);

    Optional<LaborDocente> findByUsuarioAndPeriodoAcademico(Usuario usuario, PeriodoAcademico periodoAcademico);
}
