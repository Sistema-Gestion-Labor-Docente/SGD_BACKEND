package co.edu.unicauca.sed.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.sed.api.domain.Encuesta;
import co.edu.unicauca.sed.api.domain.EncuestaRespuesta;
import co.edu.unicauca.sed.api.domain.Pregunta;

public interface EncuestaRespuestaRepository extends JpaRepository<EncuestaRespuesta, Integer> {
    Optional<EncuestaRespuesta> findByEncuestaAndPregunta(Encuesta encuesta, Pregunta pregunta);
    List<EncuestaRespuesta> findByEncuesta(Encuesta encuesta);
}
