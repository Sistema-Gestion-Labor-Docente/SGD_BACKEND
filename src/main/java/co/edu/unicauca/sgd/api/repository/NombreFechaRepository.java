package co.edu.unicauca.sgd.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.sgd.api.domain.NombreFecha;

import java.util.List;

@Repository
public interface NombreFechaRepository extends JpaRepository<NombreFecha, Integer> {
    Page<NombreFecha> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    Page<NombreFecha> findByNombreContainingIgnoreCaseAndOidNombreFechaNotIn(String nombre, List<Integer> excludedIds,
                                                                             Pageable pageable);

    Page<NombreFecha> findByOidNombreFechaNotIn(List<Integer> excludedIds, Pageable pageable);
}
