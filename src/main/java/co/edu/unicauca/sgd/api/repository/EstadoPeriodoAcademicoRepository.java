package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.EstadoPeriodoAcademico;

@Repository
public interface EstadoPeriodoAcademicoRepository extends JpaRepository<EstadoPeriodoAcademico, Integer> {
}
