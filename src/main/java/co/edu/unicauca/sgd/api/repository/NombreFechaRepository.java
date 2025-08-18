package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.NombreFecha;

@Repository
public interface NombreFechaRepository extends JpaRepository<NombreFecha, Integer> {
    Page<NombreFecha> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
