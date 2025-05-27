package co.edu.unicauca.sed.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sed.api.domain.Encuesta;
import co.edu.unicauca.sed.api.domain.EvaluacionEstudiante;

@Repository
public interface EncuestaRepository extends JpaRepository<Encuesta, Integer> {
    Optional<Encuesta> findByEvaluacionEstudiante(EvaluacionEstudiante evaluacionEstudiante);

}
