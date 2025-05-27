package co.edu.unicauca.sed.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.EstadoPeriodoAcademico;

@Repository
public interface EstadoPeriodoAcademicoRepository extends JpaRepository<EstadoPeriodoAcademico, Integer> {
}
