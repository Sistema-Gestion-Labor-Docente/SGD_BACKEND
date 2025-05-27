package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.sgd.api.domain.Pregunta;

public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {
}
