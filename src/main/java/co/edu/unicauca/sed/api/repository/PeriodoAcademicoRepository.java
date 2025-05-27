package co.edu.unicauca.sed.api.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.sed.api.domain.PeriodoAcademico;

public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, Integer> {
    Optional<PeriodoAcademico> findByEstadoPeriodoAcademicoNombre(String nombreEstado);

    boolean existsByIdPeriodo(String idPeriodo);
}
