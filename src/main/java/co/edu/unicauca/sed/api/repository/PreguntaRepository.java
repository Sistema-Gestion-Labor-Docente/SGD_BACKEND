package co.edu.unicauca.sed.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.sed.api.domain.Pregunta;

public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {
}
